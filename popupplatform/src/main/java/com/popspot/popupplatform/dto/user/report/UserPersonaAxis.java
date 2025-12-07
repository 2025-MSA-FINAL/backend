package com.popspot.popupplatform.dto.user.report;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserPersonaAxis {
    private String axisKey;
    private String axisLabel;
    private int score;         // 0~100
    private String description;
}
