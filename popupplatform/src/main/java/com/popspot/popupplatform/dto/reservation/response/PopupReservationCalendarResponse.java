package com.popspot.popupplatform.dto.reservation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "유저용 예약 캘린더 응답 DTO")
public class PopupReservationCalendarResponse {

    @Schema(description = "예약 시작일", example = "2025-09-01")
    private LocalDate startDate;

    @Schema(description = "예약 종료일", example = "2025-09-30")
    private LocalDate endDate;

    @Schema(description = "예약 가능한 날짜 목록", example = "[\"2025-09-01\",\"2025-09-02\"]")
    private List<LocalDate> availableDates;
}
