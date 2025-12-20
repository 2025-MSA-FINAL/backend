package com.popspot.popupplatform.dto.admin;

import lombok.Data;

//성별/연령별 분포 DTO
@Data
public class UserDemographicsDTO {
    private String ageGroup;      // 10대, 20대, 30대, 40대, 50대+
    private String gender;        // male, female
    private long userCount;       // 유저 수
    private Double percentage;  // 전체 대비 비율
}

