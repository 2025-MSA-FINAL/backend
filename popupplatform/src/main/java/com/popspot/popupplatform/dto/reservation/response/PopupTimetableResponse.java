package com.popspot.popupplatform.dto.reservation.response;

import com.popspot.popupplatform.dto.reservation.enums.DayOfWeekType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "요일별 예약 가능 시간표 응답 DTO")
public class PopupTimetableResponse {

    @Schema(description = "요일", example = "MON")
    private DayOfWeekType dayOfWeek;

    @Schema(description = "예약 시작 일시", example = "2025-01-10T10:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "예약 종료 일시", example = "2025-01-10T18:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "해당 요일 최대 인원", example = "20")
    private Integer capacity;
}
