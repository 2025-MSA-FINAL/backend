package com.popspot.popupplatform.dto.admin;

import lombok.Data;

// 4. 카테고리별 신고 통계 DTO
@Data
public class ReportCategoryStatsDTO {
    private Long categoryId;
    private String categoryName;
    private Long reportCount;
}
