package com.popspot.popupplatform.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.UserLimitInfoDto;
import com.popspot.popupplatform.dto.user.report.*;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserReportService {

    private final ChatClient chatClient;
    private final UserMapper userMapper;
    private final PopupMapper popupMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 후보/추천 개수 고정
    private static final int RECO_CANDIDATE_LIMIT = 60;
    private static final int RECO_FINAL_LIMIT = 6;

    public UserReportService(ChatClient.Builder builder,
                             UserMapper userMapper,
                             PopupMapper popupMapper) {

        this.userMapper = userMapper;
        this.popupMapper = popupMapper;

        // ✅ "별칭(닉네임) + 취향 포인트 + 추천 이유"만 잘 쓰게
        this.chatClient = builder
                .defaultSystem("""
                        너는 팝업 스토어 플랫폼의 '리포트 카피라이터'다.
                        입력으로 유저 행동 요약(JSON)과, 서버가 확정한 추천 팝업 목록(JSON)이 들어온다.

                        너의 역할:
                        1) 유저 성향을 '별칭(nicknameTitle)' + '상세 설명(personaDetail)'로 작성
                        2) 유저가 좋아할 만한 포인트를 3~5개 'interestPoints'로 정리
                        3) 추천 팝업들의 이유(reasons)를 1~2개씩 작성

                        절대 규칙:
                        - 추천 대상(popId), 추천 순서는 절대 바꾸지 않는다.
                        - 제공된 값 밖의 정보는 지어내지 않는다(추측 금지).
                        - personaDetail/별칭에서 조회/찜/예약 "횟수 숫자"를 나열하지 않는다. (예: 43개 조회, 29회 예약 같은 표현 금지)
                        - 문장은 과장하지 말고, 사용자 입장에서 '재밌고 공감'되게 쓴다.
                        - 별칭은 문장형 금지(“~유저입니다” 금지). 짧은 별명처럼.

                        reasons 규칙:
                        - 이유 라벨(label)은 아래 중 하나로만 사용:
                          ["해시태그","지역","연령대","성별","가격","진행상태","인기"]
                        - 이유는 반드시 '구체값'을 포함해 작성한다.
                          (예: '서울 송파구/성수동', '#주술회전', 'FREE', 'ONGOING', '서버 점수 상위권')

                        출력은 반드시 JSON 형식으로만 응답한다.
                        모든 응답은 한국어로 작성한다.
                        """)
                .build();
    }

    /**
     * ✅ 유저 리포트 (3개만)
     * 1) 유저 성향 분석 (LLM)
     * 2) 자주 가는 지역/해시태그
     * 3) 추천(서버 고정) + 이유(LLM)
     */
    public UserPersonaReport userReport(Long userId) {

        // 유저 기본 정보(성별/출생년도) - 추천 후보 demographic에만 사용
        UserLimitInfoDto limitInfo = userMapper.findUserLimitInfo(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 또는 비활성화된 유저입니다. userId=" + userId));

        String gender = limitInfo.getUserGender();         // "M" / "F" / null
        Integer birthYear = limitInfo.getUserBirthyear();  // 1998 등
        AgeInfo ageInfo = calculateAgeInfo(birthYear);

        // 유저 행동 이벤트
        List<UserPopupEventDto> eventDtos = popupMapper.selectUserPopupEvents(userId);
        if (eventDtos == null) eventDtos = Collections.emptyList();

        // 스냅샷(해시태그/지역 TOP 포함)
        UserBehaviorSnapshot snapshot = buildBehaviorSnapshot(userId, gender, ageInfo, eventDtos);

        // 2) 자주 가는 지역/해시태그
        List<UserPersonaTagStat> topHashtags = snapshot.getTopHashtags(8);
        List<UserPersonaRegionStat> topRegions = snapshot.getTopRegions(8);

        // 3) 추천(서버 고정) + 이유(LLM)
        List<UserRecommendedPopupCard> recommendations =
                buildDeterministicRecommendations(userId, snapshot, gender, birthYear);

        // 1) 유저 성향 분석(LLM)
        PersonaAndReasonsResult llmResult =
                callLlmForPersonaAndReasons(snapshot, topHashtags, topRegions, recommendations);

        // ✅ 별칭: 딱딱한 문장 대신 "재미있는 별명"
        String nicknameTitle = (llmResult != null && llmResult.nicknameTitle != null && !llmResult.nicknameTitle.isBlank())
                ? llmResult.nicknameTitle
                : buildFallbackNicknameTitle(snapshot);

        // ✅ 상세: 숫자나열 금지 → 취향/관심/동선/스타일 중심
        String personaDetail = (llmResult != null && llmResult.personaDetail != null && !llmResult.personaDetail.isBlank())
                ? llmResult.personaDetail
                : buildFallbackPersonaDetail(snapshot);

        // ✅ 관심 포인트 (프론트에서 chips로 보여주면 "AI 제대로 쓴 느낌" 강해짐)
        List<String> interestPoints = (llmResult != null && llmResult.interestPoints != null && !llmResult.interestPoints.isEmpty())
                ? llmResult.interestPoints.stream().filter(s -> s != null && !s.isBlank()).limit(5).toList()
                : buildFallbackInterestPoints(snapshot);

        // LLM reasons 적용(실패 시 서버 fallback reasons 유지)
        if (llmResult != null && llmResult.recommendationReasons != null && !llmResult.recommendationReasons.isEmpty()) {
            applyLlmReasonsToRecommendations(recommendations, llmResult.recommendationReasons);
        }

        // ✅ UserPersonaReport DTO에 필드가 이미 있다면 아래처럼 맞춰서 넣어
        // - 지금 너가 화면에 뿌리는 게 personaOneLiner/personaDetail이라면
        //   nicknameTitle을 personaOneLiner에 넣는 방식으로 연결해도 됨.
        return UserPersonaReport.builder()
                .personaOneLiner(nicknameTitle)     // ✅ 기존 필드 재활용: 1줄 요약 자리에 '별칭'
                .personaDetail(personaDetail)
                // .interestPoints(interestPoints)   // ✅ DTO에 필드 추가했다면 사용 (없으면 주석)
                .topHashtags(topHashtags)
                .topRegions(topRegions)
                .recommendations(recommendations)
                .build();
    }

    // -------------------------------------------------------
    // ✅ 1) 유저 성향 분석 + 추천 이유를 한 번에 받기
    // -------------------------------------------------------

    public static class PersonaAndReasonsResult {
        public String nicknameTitle;     // ✅ NEW: MBTI처럼 별칭
        public String personaDetail;      // ✅ 상세
        public List<String> interestPoints; // ✅ NEW: 취향 포인트

        // popId -> reasons
        public List<RecoReasonItem> recommendationReasons;
    }

    public static class RecoReasonItem {
        public Long popId;
        public List<UserRecommendationReason> reasons;
    }

    private PersonaAndReasonsResult callLlmForPersonaAndReasons(UserBehaviorSnapshot snapshot,
                                                                List<UserPersonaTagStat> topHashtags,
                                                                List<UserPersonaRegionStat> topRegions,
                                                                List<UserRecommendedPopupCard> recommendations) {

        Map<String, Object> payload = new LinkedHashMap<>();

        // ✅ LLM이 숫자나열로 도망가지 않게: 원본 행동 수치는 보내되,
        // prompt에서 "숫자 나열 금지"를 강제해둠
        Map<String, Object> behavior = new LinkedHashMap<>();
        behavior.put("totalViewCount", snapshot.getTotalViewCount());
        behavior.put("totalWishlistCount", snapshot.getTotalWishlistCount());
        behavior.put("totalReservationCount", snapshot.getTotalReservationCount());
        behavior.put("distinctPopupCount", snapshot.getDistinctPopupCount());
        behavior.put("revisitRate", snapshot.getRevisitRate());
        behavior.put("nightRate", snapshot.getNightRate());
        behavior.put("weekendRate", snapshot.getWeekendRate());
        behavior.put("priceFreeRatio", snapshot.getPriceFreeRatio());
        behavior.put("avgGroupSize", snapshot.getAvgGroupSize());

        payload.put("behavior", behavior);
        payload.put("topHashtags", topHashtags.stream().limit(5).toList());
        payload.put("topRegions", topRegions.stream().limit(5).toList());

        // 추천은 "서버가 이미 확정"된 결과만 전달
        List<Map<String, Object>> recos = new ArrayList<>();
        for (UserRecommendedPopupCard r : recommendations) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("popId", r.getPopId());
            m.put("title", r.getTitle());
            m.put("location", r.getLocation());
            m.put("priceType", r.getPriceType());
            m.put("price", r.getPrice());
            m.put("status", r.getStatus());
            m.put("serverScore", r.getServerScore());
            m.put("matchLevel", r.getMatchLevel());
            recos.add(m);
        }
        payload.put("recommendations", recos);

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }

        String prompt = """
            아래 JSON은 유저 행동 요약과, 서버가 확정한 추천 팝업 목록입니다.
            
            [문체 규칙 - 반드시 지키세요]
            - 전체 문장은 100%% 존댓말로만 작성하세요. (반말 금지)
            - 보고서 말투(“~유저입니다”, “~로 나타납니다”) 금지.
            - 문장은 자연스럽게 이어지게 작성하고, 나열형(“~하고, ~하며, ~하고…”) 2번 이상 연속 금지.
            - 같은 단어 반복 금지: "해당", "이 지역", "경향", "모습" 반복 금지.
            
            [핵심 목표]
            - '딱딱한 리포트'가 아니라, MBTI처럼 재미있는 별칭 + 공감되는 설명을 만듭니다.
            - 단, 없는 정보는 절대 지어내지 않습니다.
            
            [절대 금지]
            - 조회/찜/예약 "횟수 숫자"를 personaDetail/별칭에 직접 쓰지 마세요.
              (예: "총 43개 조회", "29회 예약" 같은 문장 금지)
            - 입력 JSON에 없는 사실(특정 작품을 좋아한다 등) 추측 금지.
            
            1) nicknameTitle (별칭)
            - 6~14자, 짧은 별칭(명사형)
            - 예시: "성수 전시 헌터", "굿즈 수집가", "주말 팝업 산책러"
            - 가능하면 topHashtags/topRegions 중 존재하는 값 1개를 '은근히' 반영 (직접 복붙 느낌 X)
            
            2) personaDetail (3~6문장, 자연스럽게 풍부하게)
            - 아래 구조를 반드시 지키세요:
              (1) 도입 1문장: 별칭을 받쳐주는 한 줄(분위기/취향)
              (2) 패턴 2~4문장: 아래 항목 중 "존재하는 데이터"만 골라 2~4개 사용해 자연스럽게 연결
                  - revisitRate / distinctPopupCount: 취향 고정 vs 탐색형
                  - nightRate / weekendRate: 언제 보는지(퇴근 후/주말형 등)
                  - priceFreeRatio: 무료 선호/가성비형 vs 유료도 OK
                  - avgGroupSize: 동행 성향(예약 기록이 있을 때만)
                  - topHashtags / topRegions: 관심 키워드, 자주 가는 동선 (존재하면 최소 1개 포함)
              (3) 마무리 1문장: “다음엔 이런 팝업도 잘 맞겠다” 같은 한 줄 제안(추측 금지, 일반적 표현만)
            
            - "지역"은 topRegions.region 문자열을 그대로 쓰되, “서울 송파구에서…”처럼 자연스럽게 녹이세요.
            - "해시태그"는 topHashtags.tag 문자열을 그대로 쓰되, “요즘은 #OOO 쪽에 반응이 빠르네요”처럼 자연스럽게.
            
            3) interestPoints
            - 3~5개
            - 사용자가 “내가 이런 걸 좋아하네?” 하고 바로 이해되는 문장 조각
            - 예: "애니/전시 테마에 반응이 빠릅니다", "송파/성수 같은 동선이 잦습니다", "무료 팝업을 선호합니다"
            - **반드시 입력 데이터로부터만** 유추 가능한 수준으로 작성
            
            4) 추천 팝업 reasons
            - recommendations 배열의 각 popId에 대해 reasons 1~2개 작성
            - label은 아래 중 하나만:
              ["해시태그","지역","연령대","성별","가격","진행상태","인기"]
            - text는 반드시 '구체값' 포함:
              - 지역: 추천 팝업 location 또는 topRegions.region
              - 해시태그: topHashtags.tag
              - 진행상태: ONGOING/UPCOMING
              - 가격: FREE/PAID 또는 price
              - 인기는 serverScore/matchLevel 근거
            
            [응답 JSON 스키마]
            {
              "nicknameTitle": "...",
              "personaDetail": "...",
              "interestPoints": ["...","..."],
              "recommendationReasons": [
                {
                  "popId": 123,
                  "reasons": [
                    {"label":"해시태그","text":"..."},
                    {"label":"지역","text":"..."}
                  ]
                }
              ]
            }
            
            입력 JSON:
            %s
            """.formatted(json);


        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(PersonaAndReasonsResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void applyLlmReasonsToRecommendations(List<UserRecommendedPopupCard> recommendations,
                                                  List<RecoReasonItem> reasonItems) {
        if (recommendations == null || recommendations.isEmpty()) return;
        if (reasonItems == null || reasonItems.isEmpty()) return;

        Map<Long, List<UserRecommendationReason>> map = new HashMap<>();
        for (RecoReasonItem item : reasonItems) {
            if (item == null || item.popId == null) continue;
            if (item.reasons == null) continue;

            List<UserRecommendationReason> clean = item.reasons.stream()
                    .filter(r -> r != null
                            && r.getLabel() != null && !r.getLabel().isBlank()
                            && r.getText() != null && !r.getText().isBlank())
                    .limit(2)
                    .toList();

            if (!clean.isEmpty()) {
                map.put(item.popId, clean);
            }
        }

        for (UserRecommendedPopupCard r : recommendations) {
            List<UserRecommendationReason> reasons = map.get(r.getPopId());
            if (reasons != null && !reasons.isEmpty()) {
                r.setReasons(reasons);
            }
        }
    }

    // -------------------------------------------------------
    // ✅ 폴백: 별칭/상세/포인트 (LLM 실패시에도 재밌게)
    // -------------------------------------------------------

    private String buildFallbackNicknameTitle(UserBehaviorSnapshot s) {
        // top tag + top region로 "별칭" 만들기
        String tag = (!s.getTopHashtags(1).isEmpty()) ? stripHash(s.getTopHashtags(1).get(0).tag()) : null;
        String region = (!s.getTopRegions(1).isEmpty()) ? s.getTopRegions(1).get(0).region() : null;

        if (tag != null && region != null) return region + " " + tag + " 덕후";
        if (tag != null) return tag + " 취향 수집가";
        if (region != null) return region + " 팝업 산책러";

        return "취향 탐색 중인 팝업러";
    }

    private String buildFallbackPersonaDetail(UserBehaviorSnapshot s) {
        String tag = (!s.getTopHashtags(1).isEmpty()) ? s.getTopHashtags(1).get(0).tag() : null;
        String region = (!s.getTopRegions(1).isEmpty()) ? s.getTopRegions(1).get(0).region() : null;

        List<String> lines = new ArrayList<>();

        if (tag != null) {
            lines.add("요즘은 " + tag + " 같은 키워드에 특히 반응하는 편이에요.");
        }
        if (region != null) {
            lines.add("동선은 " + region + " 쪽을 자주 잡는 편이라, 근처 팝업이면 더 가볍게 들를 수 있어요.");
        }

        // 행동 스타일은 숫자 대신 '경향'으로만
        if (s.getTotalReservationCount() > 0) {
            lines.add("마음에 들면 저장만 하지 않고 실제 방문까지 이어지는 타입이에요.");
        } else if (s.getTotalWishlistCount() > 0) {
            lines.add("일단 찜해두고 천천히 고르는 ‘선별형’ 스타일이에요.");
        } else {
            lines.add("여러 팝업을 둘러보며 취향 지도를 넓히는 중이에요.");
        }

        // 무료/유료는 '경향'만
        if (s.getPriceFreeRatio() >= 0.7) {
            lines.add("부담 없는 무료 팝업 위주로 새로운 테마를 가볍게 찍먹하는 편이에요.");
        }

        // 2~4문장으로 압축
        if (lines.size() <= 4) return String.join(" ", lines);
        return String.join(" ", lines.subList(0, 4));
    }

    private List<String> buildFallbackInterestPoints(UserBehaviorSnapshot s) {
        List<String> pts = new ArrayList<>();
        if (!s.getTopHashtags(1).isEmpty()) {
            pts.add(stripHash(s.getTopHashtags(1).get(0).tag()) + " 테마에 관심");
        }
        if (!s.getTopRegions(1).isEmpty()) {
            pts.add(s.getTopRegions(1).get(0).region() + " 동선 선호");
        }
        if (s.getTotalReservationCount() > 0) {
            pts.add("마음에 들면 방문까지 이어짐");
        } else if (s.getTotalWishlistCount() > 0) {
            pts.add("저장해두고 골라가는 스타일");
        } else {
            pts.add("탐색 중심으로 취향 확장 중");
        }

        if (s.getPriceFreeRatio() >= 0.7) pts.add("무료 팝업 선호");
        if (s.getRevisitRate() >= 0.6) pts.add("한 취향을 깊게 파는 편");

        return pts.stream().distinct().limit(5).toList();
    }

    private String stripHash(String tag) {
        if (tag == null) return null;
        return tag.startsWith("#") ? tag.substring(1) : tag;
    }

    // -------------------------------------------------------
    // ✅ 3) 추천: 서버가 후보군/점수/정렬 확정 (Deterministic)
    // -------------------------------------------------------

    private List<UserRecommendedPopupCard> buildDeterministicRecommendations(Long userId,
                                                                             UserBehaviorSnapshot snapshot,
                                                                             String gender,
                                                                             Integer birthYear) {

        Integer decadeStart = null;
        Integer decadeEnd = null;
        if (birthYear != null && birthYear > 0) {
            decadeStart = (birthYear / 10) * 10;
            decadeEnd = decadeStart + 9;
        }

        List<UserRecommendationCandidateDto> candidates =
                popupMapper.selectUserReportRecommendationCandidates(
                        userId,
                        gender,
                        decadeStart,
                        decadeEnd,
                        RECO_CANDIDATE_LIMIT
                );

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> myTopTags = snapshot.getTopHashtags(8).stream()
                .map(UserPersonaTagStat::tag)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> myTopRegions = snapshot.getTopRegions(5).stream()
                .map(UserPersonaRegionStat::region)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int maxPopularity = candidates.stream()
                .map(UserRecommendationCandidateDto::getPopularityScore)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        int maxDemographic = candidates.stream()
                .map(UserRecommendationCandidateDto::getDemographicScore)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        List<ScoredReco> scored = new ArrayList<>();

        for (UserRecommendationCandidateDto c : candidates) {

            int tagOverlap = countTagOverlap(myTopTags, c.getHashtagsCsv());
            int regionMatch = isRegionMatch(myTopRegions, c.getRegion()) ? 1 : 0;

            double tagScore = myTopTags.isEmpty()
                    ? 0.0
                    : clamp01((double) tagOverlap / Math.min(3, myTopTags.size()));

            double popularityScore = maxPopularity == 0 ? 0.0 : clamp01((double) nz(c.getPopularityScore()) / maxPopularity);

            double demographicScore = (gender == null || decadeStart == null)
                    ? 0.0
                    : (maxDemographic == 0 ? 0.0 : clamp01((double) nz(c.getDemographicScore()) / maxDemographic));

            double statusBoost = statusBoost(c.getStatus());

            // ✅ 서버 고정 점수(0~100)
            int finalScore = (int) Math.round(
                    55.0 * tagScore
                            + 15.0 * regionMatch
                            + 12.0 * demographicScore
                            + 10.0 * popularityScore
                            + 8.0 * statusBoost
            );
            finalScore = (int) clamp(finalScore);

            String matchLevel = (finalScore >= 75) ? "HIGH" : (finalScore >= 50 ? "MID" : "LOW");

            // LLM 실패 대비 서버 fallback reasons
            List<UserRecommendationReason> fallbackReasons = buildFallbackRecoReasons(snapshot, c, tagOverlap, regionMatch);

            UserRecommendedPopupCard card = UserRecommendedPopupCard.builder()
                    .popId(c.getPopId())
                    .thumbnailUrl(c.getThumbnailUrl())
                    .title(c.getTitle())
                    .location(c.getLocation())
                    .price(c.getPrice())
                    .priceType(c.getPriceType())
                    .status(c.getStatus())
                    .serverScore(finalScore)
                    .matchLevel(matchLevel)
                    .reasons(fallbackReasons)
                    .build();

            scored.add(new ScoredReco(finalScore, c.getPopId(), card));
        }

        // ✅ 결정적 정렬: score DESC, popId DESC
        scored.sort((a, b) -> {
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            return Long.compare(b.popId, a.popId);
        });

        return scored.stream()
                .limit(RECO_FINAL_LIMIT)
                .map(s -> s.card)
                .collect(Collectors.toList());
    }

    private record ScoredReco(int score, long popId, UserRecommendedPopupCard card) {}

    private List<UserRecommendationReason> buildFallbackRecoReasons(UserBehaviorSnapshot snapshot,
                                                                    UserRecommendationCandidateDto c,
                                                                    int tagOverlap,
                                                                    int regionMatch) {
        List<UserRecommendationReason> reasons = new ArrayList<>();

        if (tagOverlap > 0) {
            String topTag = snapshot.getTopHashtags(1).isEmpty() ? null : snapshot.getTopHashtags(1).get(0).tag();
            if (topTag != null) {
                reasons.add(new UserRecommendationReason("해시태그", topTag + " 취향이랑 결이 비슷해요."));
            } else {
                reasons.add(new UserRecommendationReason("해시태그", "최근 관심 태그와 겹치는 키워드가 있어요."));
            }
        }
        if (regionMatch == 1) {
            String topRegion = snapshot.getTopRegions(1).isEmpty() ? null : snapshot.getTopRegions(1).get(0).region();
            if (topRegion != null) {
                reasons.add(new UserRecommendationReason("지역", topRegion + " 동선에서 가기 좋아요."));
            } else {
                reasons.add(new UserRecommendationReason("지역", "자주 찾는 지역과 동선이 비슷해요."));
            }
        }

        if (reasons.isEmpty()) {
            if ("FREE".equalsIgnoreCase(c.getPriceType())) {
                reasons.add(new UserRecommendationReason("가격", "FREE라서 부담 없이 들르기 좋아요."));
            } else {
                reasons.add(new UserRecommendationReason("인기", "서버 점수/인기 기준으로 상위권이에요."));
            }
        }

        if (reasons.size() > 2) return reasons.subList(0, 2);
        return reasons;
    }

    private int countTagOverlap(Set<String> myTopTags, String hashtagsCsv) {
        if (myTopTags == null || myTopTags.isEmpty()) return 0;
        if (hashtagsCsv == null || hashtagsCsv.isBlank()) return 0;

        Set<String> tags = Arrays.stream(hashtagsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        int overlap = 0;
        for (String t : myTopTags) {
            if (tags.contains(t)) overlap++;
        }
        return overlap;
    }

    private boolean isRegionMatch(Set<String> myTopRegions, String regionKey) {
        if (myTopRegions == null || myTopRegions.isEmpty()) return false;
        if (regionKey == null || regionKey.isBlank()) return false;
        return myTopRegions.contains(regionKey.trim());
    }

    private double statusBoost(String status) {
        if (status == null) return 0.0;
        return switch (status) {
            case "ONGOING" -> 1.0;
            case "UPCOMING" -> 0.6;
            default -> 0.0;
        };
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private double clamp(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    // -------------------------------------------------------
    // ✅ 나이/연령대(추천 demographic용)
    // -------------------------------------------------------

    private AgeInfo calculateAgeInfo(Integer birthYear) {
        if (birthYear == null || birthYear <= 0) {
            return new AgeInfo(null, null, null, "연령 정보 없음");
        }

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - birthYear + 1;

        int group = (age / 10) * 10;
        String groupLabel = group + "대";

        String detailLabel;
        int mod = age % 10;
        if (mod <= 3) detailLabel = groupLabel + " 초반";
        else if (mod <= 6) detailLabel = groupLabel + " 중반";
        else detailLabel = groupLabel + " 후반";

        return new AgeInfo(age, group, detailLabel, groupLabel);
    }

    // -------------------------------------------------------
    // ✅ 행동 스냅샷(지역/해시태그 TOP 만들기 위해 유지)
    // -------------------------------------------------------

    private UserBehaviorSnapshot buildBehaviorSnapshot(Long userId,
                                                       String gender,
                                                       AgeInfo ageInfo,
                                                       List<UserPopupEventDto> eventDtos) {

        List<UserPopupEvent> events = eventDtos.stream()
                .map(dto -> {
                    UserPopupEventType type = UserPopupEventType.valueOf(dto.getEventType());
                    LocalDateTime ts = dto.getEventAt();
                    String priceType = dto.getPriceType();
                    Integer peopleCount = dto.getPeopleCount();
                    String region = dto.getRegion();

                    List<String> tags = Collections.emptyList();
                    if (dto.getHashtags() != null && !dto.getHashtags().isBlank()) {
                        tags = Arrays.stream(dto.getHashtags().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList();
                    }

                    return new UserPopupEvent(
                            dto.getPopId(),
                            type,
                            ts,
                            priceType,
                            peopleCount,
                            region,
                            tags
                    );
                })
                .toList();

        if (events.isEmpty()) {
            return UserBehaviorSnapshot.empty(userId, gender, ageInfo);
        }

        int viewCount = 0;
        int wishlistCount = 0;
        int reservationCount = 0;

        Map<Long, Integer> popupAnyActionCount = new HashMap<>();
        Map<String, Double> hashtagScore = new HashMap<>();
        Map<String, Double> regionScore = new HashMap<>();

        int freeCount = 0;
        int paidCount = 0;

        int groupVisitTotalPeople = 0;
        int groupVisitEvents = 0;

        int nightActions = 0;
        int weekendActions = 0;

        for (UserPopupEvent e : events) {

            switch (e.type()) {
                case VIEW -> viewCount++;
                case WISHLIST -> wishlistCount++;
                case RESERVATION -> reservationCount++;
            }

            popupAnyActionCount.merge(e.popId(), 1, Integer::sum);

            if ("FREE".equalsIgnoreCase(e.priceType())) freeCount++;
            else if ("PAID".equalsIgnoreCase(e.priceType())) paidCount++;

            if (e.type() == UserPopupEventType.RESERVATION && e.peopleCount() != null) {
                groupVisitTotalPeople += e.peopleCount();
                groupVisitEvents++;
            }

            int hour = e.timestamp().getHour();
            if (hour >= 20 || hour < 6) nightActions++;

            DayOfWeek dow = e.timestamp().getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) weekendActions++;

            double weight = switch (e.type()) {
                case VIEW -> 1.0;
                case WISHLIST -> 2.0;
                case RESERVATION -> 3.0;
            };

            if (e.hashtags() != null) {
                for (String tag : e.hashtags()) {
                    if (tag == null || tag.isBlank()) continue;
                    hashtagScore.merge(tag.trim(), weight, Double::sum);
                }
            }

            if (e.region() != null && !e.region().isBlank()) {
                String regionKey = normalizeRegion(e.region());
                regionScore.merge(regionKey, weight, Double::sum);
            }
        }

        int totalActions = viewCount + wishlistCount + reservationCount;
        int distinctPopupCount = popupAnyActionCount.size();

        double revisitRate;
        if (hashtagScore.isEmpty()) {
            revisitRate = 0.0;
        } else {
            List<Double> sortedTagScores = hashtagScore.values().stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();

            double totalTagScore = sortedTagScores.stream().mapToDouble(Double::doubleValue).sum();

            int topK = Math.min(3, sortedTagScores.size());
            double topKScore = 0.0;
            for (int i = 0; i < topK; i++) topKScore += sortedTagScores.get(i);

            revisitRate = (totalTagScore == 0.0) ? 0.0 : (topKScore / totalTagScore);
        }

        double nightRate = totalActions == 0 ? 0.0 : (double) nightActions / totalActions;
        double weekendRate = totalActions == 0 ? 0.0 : (double) weekendActions / totalActions;

        double priceFreeRatio = (freeCount + paidCount) == 0 ? 0.0 : (double) freeCount / (freeCount + paidCount);
        double avgGroupSize = groupVisitEvents == 0 ? 0.0 : (double) groupVisitTotalPeople / groupVisitEvents;

        List<UserPersonaTagStat> topHashtags = hashtagScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> new UserPersonaTagStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<UserPersonaRegionStat> topRegions = regionScore.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> new UserPersonaRegionStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new UserBehaviorSnapshot(
                userId,
                gender,
                ageInfo,
                viewCount,
                wishlistCount,
                reservationCount,
                distinctPopupCount,
                revisitRate,
                nightRate,
                weekendRate,
                priceFreeRatio,
                avgGroupSize,
                topHashtags,
                topRegions
        );
    }

    private String normalizeRegion(String rawRegion) {
        if (rawRegion == null || rawRegion.isBlank()) return "";
        String[] parts = rawRegion.trim().split("\\s+");
        if (parts.length >= 2) return parts[0] + " " + parts[1];
        return parts[0];
    }

    // -------------------------------------------------------
    // 내부용 record/DTO
    // -------------------------------------------------------

    private record AgeInfo(
            Integer age,
            Integer group,
            String detailLabel,
            String groupLabel
    ) {}

    public enum UserPopupEventType {
        VIEW, WISHLIST, RESERVATION
    }

    public record UserPopupEvent(
            Long popId,
            UserPopupEventType type,
            LocalDateTime timestamp,
            String priceType,
            Integer peopleCount,
            String region,
            List<String> hashtags
    ) {}

    @Getter
    private static class UserBehaviorSnapshot {

        private final Long userId;
        private final String gender;
        private final AgeInfo ageInfo;

        private final int totalViewCount;
        private final int totalWishlistCount;
        private final int totalReservationCount;

        private final int distinctPopupCount;
        private final double revisitRate;
        private final double nightRate;
        private final double weekendRate;
        private final double priceFreeRatio;
        private final double avgGroupSize;

        private final List<UserPersonaTagStat> topHashtags;
        private final List<UserPersonaRegionStat> topRegions;

        private UserBehaviorSnapshot(Long userId,
                                     String gender,
                                     AgeInfo ageInfo,
                                     int totalViewCount,
                                     int totalWishlistCount,
                                     int totalReservationCount,
                                     int distinctPopupCount,
                                     double revisitRate,
                                     double nightRate,
                                     double weekendRate,
                                     double priceFreeRatio,
                                     double avgGroupSize,
                                     List<UserPersonaTagStat> topHashtags,
                                     List<UserPersonaRegionStat> topRegions) {

            this.userId = userId;
            this.gender = gender;
            this.ageInfo = ageInfo;
            this.totalViewCount = totalViewCount;
            this.totalWishlistCount = totalWishlistCount;
            this.totalReservationCount = totalReservationCount;
            this.distinctPopupCount = distinctPopupCount;
            this.revisitRate = revisitRate;
            this.nightRate = nightRate;
            this.weekendRate = weekendRate;
            this.priceFreeRatio = priceFreeRatio;
            this.avgGroupSize = avgGroupSize;
            this.topHashtags = topHashtags;
            this.topRegions = topRegions;
        }

        public static UserBehaviorSnapshot empty(Long userId, String gender, AgeInfo ageInfo) {
            return new UserBehaviorSnapshot(
                    userId,
                    gender,
                    ageInfo,
                    0, 0, 0,
                    0,
                    0.0, 0.0, 0.0,
                    0.0,
                    0.0,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        public List<UserPersonaTagStat> getTopHashtags(int limit) {
            if (topHashtags.size() <= limit) return topHashtags;
            return topHashtags.subList(0, limit);
        }

        public List<UserPersonaRegionStat> getTopRegions(int limit) {
            if (topRegions.size() <= limit) return topRegions;
            return topRegions.subList(0, limit);
        }
    }
}
