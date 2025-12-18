package com.popspot.popupplatform.domain.reservation;

import lombok.*;

import java.time.LocalDateTime;


@Data
public class UserReservation {

    private Long urId;
    private Long popId;
    private Long ptsId;
    private Long userId;
    private LocalDateTime urDateTime;
    private Integer urUserCnt;
    private Boolean urStatus; // true = 확정
}
