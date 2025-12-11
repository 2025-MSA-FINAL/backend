package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class DailyViewStatsDTO {
    private String date;  // yyyy-MM-dd
    private int hour; //0~23
    private int views; //조회수
    private String dayLabel;  // 2/13(목)
    private String fullDate;  // yyyy-MM-dd

}
