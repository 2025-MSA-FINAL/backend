package com.popspot.popupplatform.dto.main;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MainRecommendResponse {
    private String type; // AI | POPULAR
    private List<MainRecommendPopupDto> items;
}
