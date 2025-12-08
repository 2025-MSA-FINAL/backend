package com.popspot.popupplatform.dto.reservation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "특정 날짜의 단일 타임슬롯 응답 DTO")
public class PopupTimeSlotResponse {

    @Schema(description = "슬롯 ID (현재는 더미값)", example = "1")
    private Long slotId;

    @Schema(description = "시작 시간", example = "11:00")
    private String startTime;

    @Schema(description = "종료 시간", example = "11:30")
    private String endTime;

    @Schema(description = "잔여 인원 수", example = "3")
    private Integer remainingCount;
}
