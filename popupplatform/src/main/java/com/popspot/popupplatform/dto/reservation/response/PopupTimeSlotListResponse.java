package com.popspot.popupplatform.dto.reservation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "특정 날짜의 타임슬롯 목록 응답 DTO")
public class PopupTimeSlotListResponse {

    @Schema(description = "조회 날짜", example = "2025-09-15")
    private LocalDate date;

    @Schema(description = "타임슬롯 목록")
    private List<PopupTimeSlotResponse> timeSlots;
}
