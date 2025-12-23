package com.popspot.popupplatform.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.popspot.popupplatform.common.StringOrArrayDeserializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AIReportResponseDTO {
    private String reportTitle;           // 리포트 제목
    private String executiveSummary;      // 핵심 요약 (3줄)
    private String audienceInsight;       // 고객 구성 인사이트
    private String categoryInsight;       // 카테고리 분석
    private String behaviorInsight;       // 행동 패턴 분석
    private int reportConfidence;

        // 에러 해결을 위해 추가
    private String airPdfUrl;
    private String aiContentJson; // DB에서 조회 시 air_content (JSON 문자열)을 담기 위한 필드


    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private List<String> recommendation;  // 운영 전략 제안

    private LocalDateTime generatedAt;    // 생성 시각

    public AIReportResponseDTO() {
        this.generatedAt = LocalDateTime.now();
    }
}