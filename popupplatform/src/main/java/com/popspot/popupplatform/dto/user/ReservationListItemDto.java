package com.popspot.popupplatform.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationListItemDto {

    private Long reservationId;     // USER_RESERVATION.ur_id
    private Long popupId;           // pop_id
    private String popupName;       // POPUPSTORE.pop_name
    private String popupThumbnail;  // POPUPSTORE.pop_thumbnail
    private String popupLocation;   // POPUPSTORE.pop_location

    private LocalDateTime reserveDateTime; // USER_RESERVATION.ur_date_time
    private Integer reserveUserCount;      // USER_RESERVATION.ur_user_cnt
    private Boolean reserveStatus;         // USER_RESERVATION.ur_status

    private Integer price;          // POPUPSTORE.pop_price
}
