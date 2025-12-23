package com.popspot.popupplatform.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAIReport {
    private Long airId;
    private String airType;        // MONTHLY, WEEKLY, ADHOC
    private LocalDate airPeriodStart;
    private LocalDate airPeriodEnd;
    private String airTitle;
    private String airContent;     // AI 생성 텍스트 (JSON or Summary)
    private String airGeneratedBy; // AI, ADMIN


    private String airStatus;      // DRAFT, CONFIRMED, PUBLISHED
    private String airPdfUrl;       // PDF 파일 경로 또는 URL
    private String airKpiData;     // AI 분석에 사용된 원천 데이터 (JSON String)
    private String airAdminComment; // 관리자 메모
    private Integer airViewCount;  // 조회수


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}