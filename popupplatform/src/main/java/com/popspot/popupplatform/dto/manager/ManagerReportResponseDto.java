package com.popspot.popupplatform.dto.manager;

import com.popspot.popupplatform.dto.manager.enums.AnalysisBasis;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@Schema(description = "매니저용 AI 리포트 응답 DTO")
public class ManagerReportResponseDto {

    @Schema(description = "분석 기준 (RESERVATION: 예약자 기반, WISHLIST: 찜 유저 기반)")
    private AnalysisBasis basis;

    @Schema(description = "1. 종합 성과 지표 (KPI)")
    private KpiDto kpi;

    @Schema(description = "2. 방문객 인구 통계 (Audience)")
    private AudienceDto audience;

    @Schema(description = "3. 해시태그 기반 시장 분석 (Top 3)")
    private List<MarketTrendDto> marketTrends;

    @Schema(description = "4. AI 매니저 인사이트")
    private AiInsightDto aiInsight;

    /* --- Inner Classes (섹션별 상세 구조) --- */

    @Getter
    @Builder
    public static class KpiDto {
        @Schema(description = "총 조회수", example = "1500")
        private long totalViews;

        @Schema(description = "총 찜 수", example = "120")
        private long totalWishlists;

        @Schema(description = "총 예약 확정 수", example = "45")
        private long totalReservations;

        @Schema(description = "관심도 (찜/조회 * 100)", example = "8.0")
        private double interestRate;

        @Schema(description = "예약 전환율 (예약/조회 * 100)", example = "3.0")
        private double reservationRate;

        @Schema(description = "온라인 예약 가능 여부 (false면 예약 전환율 무시)", example = "true")
        private boolean isReservationAvailable;
    }

    @Getter
    @Builder
    public static class AudienceDto {
        @Schema(description = "데이터 충분 여부 (예약자 5명 미만 시 false)", example = "true")
        private boolean hasEnoughData;

        @Schema(description = "성별 분포 (KEY: MALE, FEMALE / VALUE: 인원수)")
        private Map<String, Long> genderRatio;

        @Schema(description = "연령대 분포 (KEY: 10s, 20s... / VALUE: 인원수)")
        private Map<String, Long> ageGroupDistribution;

        @Schema(description = "방문 예정 시간대 분포 (KEY: 10(10시~12시), 12(12시~14시)... / VALUE: 인원수)")
        private Map<Integer, Long> timeSlotDistribution;
    }

    @Getter
    @Builder
    public static class MarketTrendDto {
        @Schema(description = "해시태그명", example = "#데이트")
        private String tagName;

        @Schema(description = "태그 시장 데이터 충분 여부 (50건 미만 시 false)")
        private boolean hasMarketData;

        @Schema(description = "내 팝업 지표 (예약률 또는 관심도)", example = "5.5")
        private double myMetricRate;

        @Schema(description = "시장 평균 예약 전환율", example = "8.2")
        private double marketReservationRate;

        @Schema(description = "주 타겟 연령대 비교 (Mine vs Market)", example = "{\"mine\": \"20s\", \"market\": \"30s\"}")
        private Map<String, String> topAgeGroup;

        @Schema(description = "주 타겟 성별 비교 (Mine vs Market)", example = "{\"mine\": \"FEMALE\", \"market\": \"FEMALE\"}")
        private Map<String, String> topGender;
    }

    @Getter
    @Builder
    public static class AiInsightDto {
        @Schema(description = "전체 현황 1줄 요약", example = "전반적으로 관심도는 높으나 예약 전환이 다소 낮습니다.")
        private String summary;

        @Schema(description = "시장 비교 분석 멘트", example = "#데이트 태그 시장 대비 20대 유입이 15% 부족합니다.")
        private String trendAnalysis;

        @Schema(description = "구체적 행동 제안", example = "커플 패키지 할인을 통해 20대 커플 예약을 유도해보세요.")
        private String actionSuggestion;
    }
}