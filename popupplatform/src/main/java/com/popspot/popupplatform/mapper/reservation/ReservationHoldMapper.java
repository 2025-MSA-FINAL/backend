package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.ReservationHold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface ReservationHoldMapper {

    void insert(ReservationHold hold);
}
