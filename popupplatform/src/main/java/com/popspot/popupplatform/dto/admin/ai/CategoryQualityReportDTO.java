package com.popspot.popupplatform.dto.admin.ai;

import lombok.Data;
import java.util.List;

/**
 * 카테고리별 해시태그 정합성 분석 결과 (Admin AI 내부용)
 */
@Data
public class CategoryQualityReportDTO {

    private String category;     // FOOD
    private int totalTags;
    private int matchedTags;
    private double matchRate;

    private String status;       // GOOD / WARN / BAD

    private String overview;     // AI 요약
    private List<String> details;

    private List<String> suggestedHashtags;
}
