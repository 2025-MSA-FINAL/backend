package com.popspot.popupplatform.dto.user.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRecommendationReason {
    // 예: "해시태그", "지역", "연령대", "가격", "진행상태", "인기"
    private String label;
    // 예: "#굿즈 #캐릭터 관심과 유사"
    private String text;
}