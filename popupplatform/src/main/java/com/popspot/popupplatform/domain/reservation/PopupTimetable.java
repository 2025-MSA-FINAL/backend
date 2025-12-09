package com.popspot.popupplatform.domain.reservation;

import com.popspot.popupplatform.dto.reservation.enums.DayOfWeekType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PopupTimetable {
    private Long popId;
    private DayOfWeekType ptDayOfWeek;
    private LocalDateTime ptStartDateTime;
    private LocalDateTime ptEndDateTime;
    private Integer ptCapacity;
}
