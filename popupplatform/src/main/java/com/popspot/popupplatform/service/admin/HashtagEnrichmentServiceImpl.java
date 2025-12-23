package com.popspot.popupplatform.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.domain.admin.HashtagEnriched;
import com.popspot.popupplatform.dto.admin.HashtagEnrichmentDTO;
import com.popspot.popupplatform.dto.admin.TopHashtagDTO;
import com.popspot.popupplatform.mapper.admin.HashtagEnrichedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagEnrichmentServiceImpl implements HashtagEnrichmentService {

    private static final int CACHE_TTL_DAYS = 7;  // 캐시 유효기간 7일

    private final HashtagEnrichedMapper hashtagEnrichedMapper;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<HashtagEnrichmentDTO> enrichTopHashtags(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int topN) {

        log.info("=== TOP {} 해시태그 분석 시작 (기간: {} ~ {}) ===", topN, startDate, endDate);

        // 1. TOP N 해시태그 조회
        List<TopHashtagDTO> topHashtags = hashtagEnrichedMapper.findTopHashtagsByPeriod(
                startDate, endDate, topN
        );

        if (topHashtags.isEmpty()) {
            log.warn("분석 대상 해시태그 없음");
            return new ArrayList<>();
        }

        log.info("추출된 TOP 해시태그: {} 개", topHashtags.size());

        // 2. 각 해시태그에 대해 캐시 확인 또는 AI 분석
        List<HashtagEnrichmentDTO> enrichedList = new ArrayList<>();

        for (TopHashtagDTO topHashtag : topHashtags) {
            try {
                HashtagEnrichmentDTO enriched = enrichSingleHashtag(topHashtag);
                enrichedList.add(enriched);
            } catch (Exception e) {
                log.error("해시태그 분석 실패: {} - {}", topHashtag.getHashName(), e.getMessage());
                // Fallback: 기본값으로 추가
                enrichedList.add(createFallbackEnrichment(topHashtag));
            }
        }

        log.info("=== 해시태그 분석 완료: {} / {} 성공 ===", enrichedList.size(), topHashtags.size());
        return enrichedList;
    }

    /**
     * 단일 해시태그 분석 (캐시 우선)
     */
    private HashtagEnrichmentDTO enrichSingleHashtag(TopHashtagDTO topHashtag) {
        log.debug("해시태그 분석: {}", topHashtag.getHashName());

        // 1. 캐시 확인 (TTL 유효한 것만)
        HashtagEnriched cached = hashtagEnrichedMapper.findValidCacheByHashId(topHashtag.getHashId())
                .orElse(null);

        if (cached != null) {
            log.debug("캐시 사용: {} (만료: {})", topHashtag.getHashName(), cached.getHeExpiresAt());
            return convertToDTO(cached, topHashtag.getHashName(), true);
        }

        // 2. 캐시 없음 → AI 웹 검색 수행
        log.info("AI 웹 검색 수행: {}", topHashtag.getHashName());
        HashtagEnriched newEnrichment = performAIAnalysis(topHashtag);

        // 3. DB 저장
        hashtagEnrichedMapper.insertEnrichment(newEnrichment);

        return convertToDTO(newEnrichment, topHashtag.getHashName(), false);
    }

    /**
     * AI 웹 검색 수행
     */
    private HashtagEnriched performAIAnalysis(TopHashtagDTO topHashtag) {
        String prompt = buildAnalysisPrompt(topHashtag.getHashName());

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .temperature(0.3)
                        .maxTokens(800)
                        .build())
                .call()
                .content();

        return parseAIResponse(response, topHashtag.getHashId());
    }

    /**
     * AI 분석 프롬프트 생성
     */
    private String buildAnalysisPrompt(String hashtagName) {
        return String.format("""
            당신은 팝업스토어 트렌드 분석 전문가입니다.
            
            해시태그: #%s
            
            다음을 분석해주세요:
            1. 카테고리: FOOD, FASHION, LIFESTYLE, CHARACTER, ART, TECH, ETC 중 선택
            2. 의미 요약 (50자 이내)
            3. 트렌드 이유 (왜 요즘 인기있는지, 100자 이내)
            4. 신뢰도 (0.0 ~ 1.0)
            5. 참고한 출처 타입: WEB, BLOG, NEWS, MIXED
            6. 출처 URL (최대 3개)
            
            JSON 형식으로만 응답:
            {
              "category": "FOOD",
              "summary": "카페·디저트 관련 팝업",
              "trendReason": "SNS 인증샷 문화 확산",
              "confidence": 0.85,
              "sourceType": "MIXED",
              "sourceUrls": ["https://example.com/1", "https://example.com/2"]
            }
            """, hashtagName);
    }

    /**
     * AI 응답 파싱
     */
    private HashtagEnriched parseAIResponse(String response, Long hashId) {
        try {
            // JSON 추출
            String jsonPart = extractJson(response);

            // 파싱
            var dataMap = objectMapper.readValue(jsonPart,
                    new TypeReference<java.util.Map<String, Object>>() {});

            List<String> urls = (List<String>) dataMap.getOrDefault("sourceUrls", new ArrayList<>());

            LocalDateTime now = LocalDateTime.now();

            return HashtagEnriched.builder()
                    .hashId(hashId)
                    .heCategory((String) dataMap.get("category"))
                    .heSummary((String) dataMap.get("summary"))
                    .heTrendReason((String) dataMap.get("trendReason"))
                    .heConfidence(BigDecimal.valueOf(
                            ((Number) dataMap.getOrDefault("confidence", 0.7)).doubleValue()
                    ))
                    .heSourceType((String) dataMap.get("sourceType"))
                    .heSourceUrls(objectMapper.writeValueAsString(urls))
                    .heLastAnalyzedAt(now)
                    .heExpiresAt(now.plusDays(CACHE_TTL_DAYS))
                    .build();

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", response, e);
            throw new RuntimeException("AI 분석 결과 처리 실패", e);
        }
    }

    /**
     * JSON 추출
     */
    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }

        throw new RuntimeException("JSON 형식 찾을 수 없음");
    }

    /**
     * Domain → DTO 변환
     */
    private HashtagEnrichmentDTO convertToDTO(
            HashtagEnriched enriched,
            String hashName,
            boolean cached) {

        List<String> urls = new ArrayList<>();
        try {
            if (enriched.getHeSourceUrls() != null) {
                urls = objectMapper.readValue(enriched.getHeSourceUrls(),
                        new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("URL 파싱 실패: {}", enriched.getHeSourceUrls());
        }

        return HashtagEnrichmentDTO.builder()
                .hashId(enriched.getHashId())
                .hashName(hashName)
                .category(enriched.getHeCategory())
                .summary(enriched.getHeSummary())
                .trendReason(enriched.getHeTrendReason())
                .confidence(enriched.getHeConfidence())
                .sourceType(enriched.getHeSourceType())
                .sourceUrls(urls)
                .cached(cached)
                .build();
    }

    /**
     * Fallback: 분석 실패 시 기본값
     */
    private HashtagEnrichmentDTO createFallbackEnrichment(TopHashtagDTO topHashtag) {
        return HashtagEnrichmentDTO.builder()
                .hashId(topHashtag.getHashId())
                .hashName(topHashtag.getHashName())
                .category("ETC")
                .summary("분석 데이터 부족")
                .trendReason("분석 불가")
                .confidence(BigDecimal.ZERO)
                .sourceType("NONE")
                .sourceUrls(new ArrayList<>())
                .cached(false)
                .build();
    }
}
