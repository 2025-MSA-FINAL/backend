package com.popspot.popupplatform.dto.main;

import lombok.Data;

import java.util.List;

@Data
public class UserTasteProfileDto {

    /** 최근 본 팝업 이름 */
    private List<String> recentViewedNames;

    /** 선호 해시태그 Top */
    private List<String> topTags;

    /** 무료/유료 선호도 힌트 */
    private String pricePreference; // FREE / PAID / MIXED
}
