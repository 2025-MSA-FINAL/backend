package com.popspot.popupplatform.dto.reservation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "예약 제외일(예외일) 응답 DTO")
public class PopupExcludeDateResponse {

    @Schema(description = "예약 제외일", example = "2025-01-12")
    private LocalDate date;
}
