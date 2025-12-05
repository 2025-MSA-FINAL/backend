package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewHeatmapDTO {
    private String day;       // "2/13(목)"
    private String fullDate;  // "2025-02-13"
    private int hour;         // 0~23
    private int views;
}


