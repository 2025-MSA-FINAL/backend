package com.popspot.popupplatform.dto.main;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MainRecommendPopupDto {
    private Long popId;
    private String popName;
    private String popThumbnail;
    private String popLocation;
    private LocalDateTime popStartDate;
    private LocalDateTime popEndDate;
    private String popPriceType;
}
