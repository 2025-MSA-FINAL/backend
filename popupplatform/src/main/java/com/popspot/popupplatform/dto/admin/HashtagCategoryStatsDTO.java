package com.popspot.popupplatform.dto.admin;

import lombok.Data;

/**
 * 해시태그 카테고리별 통계 DTO
 * AI 리포트 및 관리자 대시보드용
 */
@Data
public class HashtagCategoryStatsDTO {
    private String mainCategory;      // ANIMATION, FOOD, FASHION, BEAUTY
    private String subCategory;       // GOODS, EXHIBITION, CAFE, etc.
    private Long popupCount;          // 해당 카테고리 팝업 수
    private Long wishlistCount;       // 찜 수
    private Long viewCount;           // 조회 수
    private String category;
    private String hashtag;
    private int count;
}