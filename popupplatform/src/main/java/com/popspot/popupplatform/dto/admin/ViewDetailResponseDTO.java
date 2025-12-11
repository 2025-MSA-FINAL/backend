package com.popspot.popupplatform.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class ViewDetailResponseDTO {

    private String fullDate; // yyyy-MM-dd
    private int hour;        // 0~23
    private int totalViews;

    private List<ViewDetailPopupDTO> topPopups;
    private List<ViewDetailGenderDTO> genderStats;
    private List<ViewDetailAgeDTO> ageStats;
}
