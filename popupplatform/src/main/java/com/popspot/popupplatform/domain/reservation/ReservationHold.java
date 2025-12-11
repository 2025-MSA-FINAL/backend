package com.popspot.popupplatform.domain.reservation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReservationHold {

    private Long rhId;
    private Long ptsId;
    private Long userId;
    private LocalDate rhDate;
    private Integer rhUserCnt;
    private String rhStatus;        // ACTIVE / USED / CANCELED / EXPIRED
    private LocalDateTime rhExpiresAt;
}
