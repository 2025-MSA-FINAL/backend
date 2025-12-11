package com.popspot.popupplatform.domain.reservation;

import com.popspot.popupplatform.dto.reservation.enums.EntryTimeUnit;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PopupReservation {
    private Long popId;
    private LocalDateTime prStartTime;
    private LocalDateTime prEndTime;
    private Integer prMaxUserCnt;
    private EntryTimeUnit prEntryTimeUnit;
}