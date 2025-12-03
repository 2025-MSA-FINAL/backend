package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupReservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PopupReservationMapper {

    int insertPopupReservation(PopupReservation reservation);

    PopupReservation findByPopId(Long popId);
}
