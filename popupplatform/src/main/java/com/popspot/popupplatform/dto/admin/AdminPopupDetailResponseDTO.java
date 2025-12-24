package com.popspot.popupplatform.dto.admin;

import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPopupDetailResponseDTO {
    // 기본 정보
    private Long popId;
    private Long popOwnerId;
    private String popName;
    private String popDescription;
    private String popThumbnail;
    private String popLocation;
    private Double popLatitude;
    private Double popLongitude;
    private LocalDateTime popStartDate;
    private LocalDateTime popEndDate;
    private Boolean popIsReservation;
    private PopupPriceType popPriceType;
    private Integer popPrice;
    private PopupStatus popStatus;
    private String popInstaUrl;
    private Boolean popModerationStatus;
    private Boolean popIsDeleted;
    private Long popViewCount;
    private String popAiSummary;

    // 운영자 정보
    private String ownerName;
    private String ownerEmail;

    // 통계 정보
    private Long reservationCount;
    private Long wishlistCount;

    // 날짜 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}