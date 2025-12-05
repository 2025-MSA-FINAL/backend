package com.popspot.popupplatform.dto.reservation.request;

import com.popspot.popupplatform.dto.reservation.enums.EntryTimeUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "팝업 예약 기본 설정 (단위 시간, 운영 기간, 계정당 예약 최대 인원)")
public class PopupReservationRequest {

    @Schema(
            description = "예약 단위 시간",
            example = "MIN30"
    )
    private EntryTimeUnit entryTimeUnit;  // ALL_DAY / MIN30 / HOUR1

    @Schema(
            description = "팝업 예약 시작 일시",
            example = "2025-01-10T00:00:00"
    )
    private LocalDateTime startDate;

    @Schema(
            description = "팝업 예약 종료 일시",
            example = "2025-01-20T23:59:59"
    )
    private LocalDateTime endDate;

    @Schema(
            description = "계정당 예약 최대 인원",
            example = "100"
    )
    private Integer maxUserCnt;
}
