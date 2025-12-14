package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.ReservationHold;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ReservationHoldMapper {

    void insert(ReservationHold hold);
}
