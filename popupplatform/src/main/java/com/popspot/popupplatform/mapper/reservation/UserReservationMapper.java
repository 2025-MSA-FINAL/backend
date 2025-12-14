package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.UserReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface UserReservationMapper {


    void insert(UserReservation reservation);
}
