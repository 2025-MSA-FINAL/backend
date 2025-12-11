package com.popspot.popupplatform.dto.admin;

import lombok.Data;

//인기 팝업 DTO
@Data
public class PopularPopupDTO {
    private Long popId;
    private String popName;
    private String popThumbnail;
    private Long viewCount;       // 조회수
    private int popupRank;             // 순위
}
