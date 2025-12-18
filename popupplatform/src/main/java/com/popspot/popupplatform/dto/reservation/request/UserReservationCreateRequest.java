package com.popspot.popupplatform.dto.reservation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "사용자 예약 생성 요청")
public class UserReservationCreateRequest {

    @Schema(description = "팝업 ID", example = "1071")
    private Long popupId;

    @Schema(description = "예약 슬롯 ID (POPUP_TIME_SLOT.pts_id)", example = "31")
    private Long slotId;

    @Schema(description = "예약 날짜 (yyyy-MM-dd)", example = "2025-12-09")
    private LocalDate date;

    @Schema(description = "예약 인원 수", example = "3")
    private Integer people;
}
