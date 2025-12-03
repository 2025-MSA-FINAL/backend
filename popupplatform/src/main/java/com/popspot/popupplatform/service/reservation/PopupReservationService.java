package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.dto.reservation.request.PopupReservationSettingRequest;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationSettingResponse;

public interface PopupReservationService {

    // 전체 저장 (create + update 개념 통합)
    PopupReservationSettingResponse saveReservationSetting(Long popId, PopupReservationSettingRequest request);

    // 조회
    PopupReservationSettingResponse getReservationSetting(Long popId);
}
