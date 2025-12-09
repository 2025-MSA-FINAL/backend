package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.dto.reservation.request.PopupReservationSettingRequest;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationCalendarResponse;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationSettingResponse;
import com.popspot.popupplatform.dto.reservation.response.PopupTimeSlotListResponse;

import java.time.LocalDate;

public interface PopupReservationService {

    // 전체 저장 (create + update 개념 통합)
    PopupReservationSettingResponse saveReservationSetting(Long popId, PopupReservationSettingRequest request);

    // 조회
    PopupReservationSettingResponse getReservationSetting(Long popId);

    // 🔹 추가 1: 캘린더/일자 정보 조회
    PopupReservationCalendarResponse getReservationCalendar(Long popId);

    // 🔹 추가 2: 특정 날짜의 타임슬롯 목록 조회
    PopupTimeSlotListResponse getTimeSlotsByDate(Long popId, LocalDate date);
}
