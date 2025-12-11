package com.popspot.popupplatform.dto.reservation.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 단위 시간")
public enum EntryTimeUnit {

    @Schema(description = "상시 예약 (시간 구분 없음)")
    ALL_DAY,

    @Schema(description = "30분 단위 예약")
    MIN30,

    @Schema(description = "1시간 단위 예약")
    HOUR1
}

