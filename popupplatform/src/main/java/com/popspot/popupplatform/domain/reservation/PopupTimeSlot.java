package com.popspot.popupplatform.domain.reservation;

import com.popspot.popupplatform.dto.reservation.enums.DayOfWeekType;
import lombok.Data;

import java.time.LocalTime;

@Data
public class PopupTimeSlot {

    private Long ptsId;
    private Long popId;

    private DayOfWeekType ptsDayOfWeek;

    private LocalTime ptsStartTime;
    private LocalTime ptsEndTime;

    private Integer ptsCapacity;
}
