package com.popspot.popupplatform.dto.reservation.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요일 타입 (월~일)")
public enum DayOfWeekType {

    @Schema(description = "월요일")
    MON,

    @Schema(description = "화요일")
    TUE,

    @Schema(description = "수요일")
    WED,

    @Schema(description = "목요일")
    THU,

    @Schema(description = "금요일")
    FRI,

    @Schema(description = "토요일")
    SAT,

    @Schema(description = "일요일")
    SUN
}
