package com.popspot.popupplatform.dto.user.report;

import lombok.Data;

@Data
public class UserRecommendationCandidateDto {
    private Long popId;
    private String thumbnailUrl;
    private String title;
    private String location;
    private Integer price;
    private String priceType;
    private String status;

    // 정규화된 지역(앞 2토큰) - SQL에서 미리 잘라줌
    private String region;

    // 콤마로 연결된 해시태그 목록
    private String hashtagsCsv;

    // popularity signals (이미 프로젝트에서 쓰던 방식 재사용)
    private Integer popularityScore;

    // demographic signals
    private Integer demographicScore;
}
