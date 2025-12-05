package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class MonthlyUserGrowthDTO {
    private String month;   // yyyy-MM
    private long newUsers;  // 신규 가입자 수
}

