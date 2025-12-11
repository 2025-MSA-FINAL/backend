package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class PopupStoreListDTO {
    private Long popId;
    private String popName;
    private String popThumbnail;
    private String popLocation;
    private String popStartDate;
    private String popEndDate;
    private String popStatus;          // upcoming, active, ended
    private Boolean popModerationStatus; // true: 승인, false: 미승인, null: 대기
    private Boolean popIsDeleted;
    private Long popViewCount;
    private String popPriceType;       // free, paid
    private Integer popPrice;

    // 운영자 정보
    private Long popOwnerId;
    private String ownerName;
    private String ownerEmail;

    // 예약 정보
    private Boolean popIsReservation;
    private Long reservationCount;     // 예약 수

    // 통계
    private Long wishlistCount;        // 찜한 유저 수

    private String createdAt;
}
