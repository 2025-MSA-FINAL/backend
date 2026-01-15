package com.popspot.popupplatform.dto.user.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserPersonaReport {

    // 1) 유저 성향 분석
    private String personaOneLiner;   // 예: "당신은 신상 팝업을 빠르게 찍먹하는 탐색형"
    private String personaDetail;     // 예: 2~4문장 상세 설명(LLM)

    // 2) 자주 가는 지역 및 해시태그
    private List<UserPersonaTagStat> topHashtags;
    private List<UserPersonaRegionStat> topRegions;

    // 3) 추천 + 이유 (서버가 popId 결정, LLM이 이유 작성)
    private List<UserRecommendedPopupCard> recommendations;
}
