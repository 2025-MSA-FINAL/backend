package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.UserReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

@Mapper
public interface UserReservationMapper {

    /**
     * 해당 슬롯 + 날짜 범위에서 확정된 예약 인원 합계
     */
    Integer sumConfirmedUserCount(
            @Param("ptsId") Long ptsId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Integer sumConfirmedUserCountForDisplay(
            @Param("ptsId") Long ptsId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void insert(UserReservation reservation);
}
