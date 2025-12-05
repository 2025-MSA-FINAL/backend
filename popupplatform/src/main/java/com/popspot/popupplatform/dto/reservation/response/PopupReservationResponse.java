package com.popspot.popupplatform.dto.reservation.response;

import com.popspot.popupplatform.dto.reservation.enums.EntryTimeUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "팝업 예약 기본 설정 응답 DTO")
public class PopupReservationResponse {

    @Schema(description = "예약 단위 시간", example = "MIN30")
    private EntryTimeUnit entryTimeUnit;

    @Schema(description = "예약 시작 일시", example = "2025-01-10T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "예약 종료 일시", example = "2025-01-20T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "계정당 예약 최대 인원", example = "100")
    private Integer maxUserCnt;
}
