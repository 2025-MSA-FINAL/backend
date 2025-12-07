package com.popspot.popupplatform.dto.user.report;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserPersonaPopupCard {
    private Long popId;
    private String thumbnailUrl;
    private String title;
    private String location;
    private Integer price;
    private String priceType;  // FREE / PAID ...
    private String status;     // UPCOMING / ONGOING ...   // UPCOMING / ONGOING ...
}
