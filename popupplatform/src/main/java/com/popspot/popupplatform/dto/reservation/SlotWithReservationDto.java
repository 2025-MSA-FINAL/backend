package com.popspot.popupplatform.dto.reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "타임슬롯 + 팝업 기본 예약 설정 통합 DTO")
public class SlotWithReservationDto {

    @Schema(description = "타임슬롯 ID", example = "12")
    private Long slotId;

    @Schema(description = "팝업 ID", example = "3")
    private Long popId;

    @Schema(description = "예약 가능한 시작 시간", example = "10:00")
    private LocalTime startTime;

    @Schema(description = "해당 슬롯의 최대 수용 인원", example = "40")
    private Integer capacity;

    @Schema(description = "계정당 예약 가능 최대 인원 (pr_max_user_cnt)", example = "5")
    private Integer maxUserCnt; // pr_max_user_cnt
}
