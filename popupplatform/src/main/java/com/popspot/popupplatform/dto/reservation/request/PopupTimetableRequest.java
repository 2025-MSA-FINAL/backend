package com.popspot.popupplatform.dto.reservation.request;

import com.popspot.popupplatform.dto.reservation.enums.DayOfWeekType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "요일별 예약 가능 시간 및 요일별 최대 인원")
public class PopupTimetableRequest {

    @Schema(
            description = "요일",
            example = "MON"
    )
    private DayOfWeekType dayOfWeek;

    @Schema(
            description = "해당 요일의 예약 시작 시각",
            example = "10:00"
    )
    private LocalTime startTime;

    @Schema(
            description = "해당 요일의 예약 종료 시각",
            example = "18:00"
    )
    private LocalTime endTime;

    @Schema(
            description = "해당 요일 예약 가능 최대 인원",
            example = "20"
    )
    private Integer capacity;
}
