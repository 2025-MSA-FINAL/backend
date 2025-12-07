package com.popspot.popupplatform.service.manager;

import com.popspot.popupplatform.dto.manager.enums.AnalysisBasis;
import com.popspot.popupplatform.dto.manager.ManagerReportResponseDto;
import com.popspot.popupplatform.dto.manager.ManagerReportResponseDto.*;
import com.popspot.popupplatform.mapper.manager.ManagerReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ManagerReportService {

    private final ManagerReportMapper managerReportMapper;
    private final ChatClient chatClient;

    // [설정] 통계 노출 기준값
    private static final int MIN_AUDIENCE_THRESHOLD = 0;
    private static final long MIN_VIEWS_THRESHOLD = 0L;

    public ManagerReportService(ManagerReportMapper managerReportMapper, ChatClient.Builder builder) {
        this.managerReportMapper = managerReportMapper;
        this.chatClient = builder
                .defaultSystem("""
                    당신은 팝업 스토어 성과를 분석해주는 'AI 수석 매니저'입니다.
                    제공된 통계 데이터(조회수, 예약률, 시장 비교 등)를 바탕으로 사장님에게 보고할 리포트를 작성하세요.
                    
                    [출력 규칙]
                    1. 3줄로 출력 (요약, 시장 분석, 액션 제안)
                    2. 각 줄은 [요약], [시장 분석], [액션 제안] 접두사 필수.
                    3. 정중한 전문가 톤 유지.
                """)
                .build();
    }

    public ManagerReportResponseDto getManagerReport(Long popupId) {
        // 1. KPI 조회
        KpiDto kpiDto = generateKpiStats(popupId);

        // 2. 분석 기준 결정 (RESERVATION vs WISHLIST)
        AnalysisBasis basis = determineBasis(kpiDto);

        // 3. Audience 생성
        // (MarketTrend에서 이 데이터를 써야 하므로 먼저 생성)
        AudienceDto audienceDto = (basis == AnalysisBasis.RESERVATION)
                ? generateReservationAudience(popupId)
                : generateWishlistAudience(popupId);

        // 4. Market Trend 생성 (Audience 데이터 주입하여 '내 타겟' 분석)
        List<MarketTrendDto> marketTrends = generateMarketTrends(popupId, basis, audienceDto);

        // 5. AI 인사이트
        AiInsightDto aiInsightDto = generateAiInsight(kpiDto, audienceDto, marketTrends, basis);

        return ManagerReportResponseDto.builder()
                .basis(basis)
                .kpi(kpiDto)
                .audience(audienceDto)
                .marketTrends(marketTrends)
                .aiInsight(aiInsightDto)
                .build();
    }

    // ==========================================
    // 로직: 분석 기준 결정
    // ==========================================
    private AnalysisBasis determineBasis(KpiDto kpi) {
        if (!kpi.isReservationAvailable() || kpi.getTotalReservations() == 0) {
            return AnalysisBasis.WISHLIST;
        }
        return AnalysisBasis.RESERVATION;
    }

    // ==========================================
    // 1. KPI
    // ==========================================
    private KpiDto generateKpiStats(Long popupId) {
        Map<String, Object> stats = managerReportMapper.selectKpiStats(popupId);
        long views = parseLong(stats.get("views"));
        long wishlists = parseLong(stats.get("wishlists"));
        long reservations = parseLong(stats.get("reservations"));
        boolean isReservationAvailable = Boolean.TRUE.equals(stats.get("isReservationAvailable"));

        double interestRate = (views == 0) ? 0.0 : ((double) wishlists / views) * 100.0;
        double reservationRate = (views == 0) ? 0.0 : ((double) reservations / views) * 100.0;

        return KpiDto.builder()
                .totalViews(views)
                .totalWishlists(wishlists)
                .totalReservations(reservations)
                .interestRate(formatDouble(interestRate))
                .reservationRate(formatDouble(reservationRate))
                .isReservationAvailable(isReservationAvailable)
                .build();
    }

    // ==========================================
    // 2. Audience
    // ==========================================
    private AudienceDto generateReservationAudience(Long popupId) {
        return buildAudienceDto(
                managerReportMapper.selectAudienceGender(popupId),
                managerReportMapper.selectAudienceAge(popupId),
                managerReportMapper.selectAudienceTime(popupId)
        );
    }

    private AudienceDto generateWishlistAudience(Long popupId) {
        return buildAudienceDto(
                managerReportMapper.selectAudienceGenderByWishlist(popupId),
                managerReportMapper.selectAudienceAgeByWishlist(popupId),
                managerReportMapper.selectAudienceTimeByWishlist(popupId)
        );
    }

    private AudienceDto buildAudienceDto(List<Map<String, Object>> genderList, List<Map<String, Object>> ageList, List<Map<String, Object>> timeList) {
        long total = genderList.stream().mapToLong(m -> parseLong(m.get("count"))).sum();
        boolean hasEnoughData = total >= MIN_AUDIENCE_THRESHOLD;

        Map<String, Long> genderMap = new HashMap<>();
        genderList.forEach(m -> genderMap.put((String) m.get("gender"), parseLong(m.get("count"))));

        Map<String, Long> ageMap = new HashMap<>();
        ageList.forEach(m -> ageMap.put((String) m.get("ageGroup"), parseLong(m.get("count"))));

        Map<Integer, Long> timeMap = new HashMap<>();
        timeList.forEach(m -> timeMap.put((Integer) m.get("hour"), parseLong(m.get("count"))));

        return AudienceDto.builder()
                .hasEnoughData(hasEnoughData)
                .genderRatio(genderMap)
                .ageGroupDistribution(ageMap)
                .timeSlotDistribution(timeMap)
                .build();
    }

    // ==========================================
    // 3. Market Trend (고도화: Audience 데이터 활용)
    // ==========================================
    private List<MarketTrendDto> generateMarketTrends(Long popupId, AnalysisBasis basis, AudienceDto audienceDto) {
        List<MarketTrendDto> trends = new ArrayList<>();
        List<String> hashtags = managerReportMapper.selectMeaningfulHashtags(popupId);

        // [고도화] 내 팝업의 주 타겟(Top 1) 추출 로직
        // AudienceDto에 있는 Map 데이터를 스트림으로 돌려서 Value가 제일 큰 Key를 찾음
        String myTopGender = getTopKey(audienceDto.getGenderRatio());
        String myTopAge = getTopKey(audienceDto.getAgeGroupDistribution());

        for (String tag : hashtags) {
            Map<String, Object> marketStats = managerReportMapper.selectMarketTrendStats(tag);

            Map<String, Object> myStats = (basis == AnalysisBasis.RESERVATION)
                    ? managerReportMapper.selectMyPopupTrendStats(popupId, tag)
                    : managerReportMapper.selectMyPopupTrendStatsByWishlist(popupId, tag);

            if (marketStats == null || myStats == null) continue;

            double marketRate = parseDouble(marketStats.get("marketReservationRate"));
            double myRate = parseDouble(myStats.get("myRate"));

            // [비교 데이터 조립]
            Map<String, String> topGenderMap = new HashMap<>();
            topGenderMap.put("market", (String) marketStats.get("topGender")); // 시장 1위
            topGenderMap.put("mine", myTopGender);                             // 내 팝업 1위 (계산됨)

            Map<String, String> topAgeMap = new HashMap<>();
            topAgeMap.put("market", (String) marketStats.get("topAgeGroup"));  // 시장 1위
            topAgeMap.put("mine", myTopAge);                                   // 내 팝업 1위 (계산됨)

            trends.add(MarketTrendDto.builder()
                    .tagName(tag)
                    .hasMarketData(marketRate > 0)
                    .marketReservationRate(formatDouble(marketRate))
                    .myMetricRate(formatDouble(myRate))
                    .topGender(topGenderMap)
                    .topAgeGroup(topAgeMap)
                    .build());
        }
        return trends;
    }

    // ==========================================
    // 4. AI Insight
    // ==========================================
    private AiInsightDto generateAiInsight(KpiDto kpi, AudienceDto audience, List<MarketTrendDto> trends, AnalysisBasis basis) {
        if (kpi.getTotalViews() < MIN_VIEWS_THRESHOLD) {
            return AiInsightDto.builder()
                    .summary("아직 데이터가 충분하지 않습니다.")
                    .trendAnalysis("조회수와 관심 데이터가 쌓이면 분석을 시작합니다.")
                    .actionSuggestion("SNS 홍보를 통해 초기 유입을 늘려보세요.")
                    .build();
        }

        String basisLabel = (basis == AnalysisBasis.RESERVATION) ? "예약 확정자 기준" : "찜(관심) 유저 기준";

        String audienceText = "데이터 부족";
        if (audience != null && audience.isHasEnoughData()) {
            audienceText = String.format(
                    "기준: %s, 성별분포: %s, 연령대 분포: %s",
                    basisLabel,
                    audience.getGenderRatio(),
                    audience.getAgeGroupDistribution()
            );
        }

        String trendText;
        if (trends.isEmpty()) {
            trendText = "비교할 시장 데이터 없음";
        } else {
            String myLabel = (basis == AnalysisBasis.RESERVATION) ? "내 예약률" : "내 관심도";
            trendText = trends.stream().limit(3)
                    .map(t -> String.format(
                            "#%s 태그 시장: 주 타겟 [%s %s] / 시장 평균 예약률 %.1f%% vs %s %.1f%% (내 주 타겟: %s %s)",
                            t.getTagName(),
                            t.getTopAgeGroup().get("market"), t.getTopGender().get("market"),
                            t.getMarketReservationRate(), myLabel, t.getMyMetricRate(),
                            t.getTopAgeGroup().get("mine"), t.getTopGender().get("mine") // AI에게 내 타겟 정보도 전달
                    ))
                    .collect(Collectors.joining("\n"));
        }

        try {
            String kpiText;
            if (basis == AnalysisBasis.RESERVATION) {
                kpiText = String.format("- 조회수: %d, 찜: %d, 예약: %d (전환율 %.1f%%)",
                        kpi.getTotalViews(), kpi.getTotalWishlists(), kpi.getTotalReservations(), kpi.getReservationRate());
            } else {
                kpiText = String.format("- 조회수: %d, 찜: %d (관심도 %.1f%%), 예약기능: 미사용",
                        kpi.getTotalViews(), kpi.getTotalWishlists(), kpi.getInterestRate());
            }

            String userPrompt = String.format("""
                    [분석 기준: %s]
                    [KPI]
                    %s
                    
                    [인구통계]
                    %s
                    
                    [시장비교]
                    %s
                    
                    위 데이터를 바탕으로 요약/분석/제안 3줄 리포트를 작성해줘.
                    """,
                    basisLabel, kpiText, audienceText, trendText
            );

            String response = chatClient.prompt().user(userPrompt).call().content();
            String[] lines = response.split("\n");

            String summary = "AI 분석 중";
            String analysis = "시장 데이터 분석 중";
            String suggestion = "기본적인 관리에 집중하세요.";

            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("[요약]")) summary = line;
                else if (line.startsWith("[시장 분석]")) analysis = line;
                else if (line.startsWith("[액션 제안]")) suggestion = line;
            }

            return AiInsightDto.builder()
                    .summary(summary)
                    .trendAnalysis(analysis)
                    .actionSuggestion(suggestion)
                    .build();
        } catch (Exception e) {
            log.error("AI Error", e);
            return AiInsightDto.builder().summary("AI 서버 연결 지연").trendAnalysis("잠시 후 시도").actionSuggestion("기본 관리 권장").build();
        }
    }

    // ==========================================
    // 헬퍼 메서드 (고도화됨)
    // ==========================================

    // Map에서 Value가 가장 큰 Key를 찾는 메서드 (예: "FEMALE", "20s")
    private String getTopKey(Map<String, Long> map) {
        if (map == null || map.isEmpty()) return "데이터 부족";

        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("데이터 부족");
    }

    private long parseLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.parseLong(obj.toString()); } catch (Exception e) { return 0L; }
    }
    private double parseDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try { return Double.parseDouble(obj.toString()); } catch (Exception e) { return 0.0; }
    }
    private double formatDouble(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}