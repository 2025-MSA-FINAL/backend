package com.popspot.popupplatform.dto.reservation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "예약 제외일(예외일) 정보")
public class PopupExcludeDateRequest {

    @Schema(
            description = "예약 제외일 (해당 날짜는 예약 불가)",
            example = "2025-01-12"
    )
    private LocalDate date;
}
