package com.popspot.popupplatform.domain.reservation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserReservation {

    private Long urId;
    private Long popId;
    private Long ptsId;
    private Long userId;
    private LocalDateTime urDateTime;
    private Integer urUserCnt;
    private Boolean urStatus; // true = 확정
}
