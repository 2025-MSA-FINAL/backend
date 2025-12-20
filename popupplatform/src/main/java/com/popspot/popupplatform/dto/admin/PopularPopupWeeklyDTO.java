package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class PopularPopupWeeklyDTO {
    private Long popId;
    private String popName;
    private String popThumbnail;
    private int viewCount;
    private int popupRank;
    private String category;
}