package com.popspot.popupplatform.dto.reservation.response;

import com.popspot.popupplatform.domain.reservation.PopupBlock;
import com.popspot.popupplatform.domain.reservation.PopupReservation;
import com.popspot.popupplatform.domain.reservation.PopupTimetable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "팝업 예약 설정 전체 응답 DTO")
public class PopupReservationSettingResponse {

    @Schema(description = "예약 기본 설정 정보")
    private PopupReservationResponse reservationInfo;

    @Schema(description = "요일별 시간표 목록")
    private List<PopupTimetableResponse> timetables;

    @Schema(description = "예약 제외일 목록")
    private List<PopupExcludeDateResponse> excludeDates;

    public static PopupReservationSettingResponse of(
            PopupReservation reservation,
            List<PopupTimetable> timetableList,
            List<PopupBlock> blockList
    ) {
        return PopupReservationSettingResponse.builder()
                .reservationInfo(
                        PopupReservationResponse.builder()
                                .entryTimeUnit(reservation.getPrEntryTimeUnit())
                                .startDate(reservation.getPrStartTime())
                                .endDate(reservation.getPrEndTime())
                                .maxUserCnt(reservation.getPrMaxUserCnt())
                                .build()
                )
                .timetables(
                        timetableList.stream()
                                .map(tt -> PopupTimetableResponse.builder()
                                        .dayOfWeek(tt.getPtDayOfWeek())
                                        .startDateTime(tt.getPtStartDateTime())
                                        .endDateTime(tt.getPtEndDateTime())
                                        .capacity(tt.getPtCapacity())
                                        .build())
                                .collect(Collectors.toList())
                )
                .excludeDates(
                        blockList.stream()
                                .map(b -> PopupExcludeDateResponse.builder()
                                        .date(b.getPbDateTime().toLocalDate())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
