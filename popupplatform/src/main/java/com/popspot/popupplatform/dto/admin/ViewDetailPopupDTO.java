package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class ViewDetailPopupDTO {
    private Long popId;
    private String popName;
    private String thumbnail;
    private int viewCount;
}
