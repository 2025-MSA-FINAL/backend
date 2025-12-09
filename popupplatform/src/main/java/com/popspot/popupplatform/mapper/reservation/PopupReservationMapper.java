package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupReservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PopupReservationMapper {

    /**
     * POPUP_RESERVATION upsert
     * - pop_id 를 PK 로 사용
     * - 있으면 UPDATE, 없으면 INSERT
     */
    int insertPopupReservation(PopupReservation reservation);

    /**
     * 팝업별 예약 설정 조회
     */
    PopupReservation findByPopId(Long popId);
}
