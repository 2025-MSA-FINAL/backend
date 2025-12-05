package com.popspot.popupplatform.dto.reservation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "팝업 예약 설정 전체 정보 (기본 설정 + 요일별 시간표 + 제외일)")
public class PopupReservationSettingRequest {

    @Schema(
            description = "기본 예약 설정 정보 (단위 시간, 운영 기간, 전체 최대 인원)"
    )
    private PopupReservationRequest reservationInfo;

    @Schema(
            description = "요일별 예약 가능 시간표 목록",
            example = "[{\"dayOfWeek\":\"MON\",\"startTime\":\"10:00\",\"endTime\":\"18:00\",\"capacity\":20}]"
    )
    private List<PopupTimetableRequest> timetables;

    @Schema(
            description = "예약 제외일 목록",
            example = "[{\"date\":\"2025-01-12\"},{\"date\":\"2025-01-15\"}]"
    )
    private List<PopupExcludeDateRequest> excludeDates;
}
