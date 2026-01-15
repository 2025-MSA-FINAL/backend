package com.popspot.popupplatform.dto.user.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserRecommendedPopupCard {
    private Long popId;
    private String thumbnailUrl;
    private String title;
    private String location;
    private Integer price;
    private String priceType;
    private String status;

    // 서버 고정 추천 점수/레벨(면접에서 “LLM이 추천하는 게 아니다” 증명용)
    private Integer serverScore;   // 0~100
    private String matchLevel;     // HIGH / MID / LOW

    // LLM이 만들어주는 "이유" (서버가 만든 후보/점수는 절대 변경 불가)
    private List<UserRecommendationReason> reasons;
}
