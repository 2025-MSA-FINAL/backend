package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.ReservationHold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface ReservationHoldMapper {

    /**
     * 해당 슬롯 + 날짜의 ACTIVE HOLD 인원 합계 (만료 안된 것만)
     */
    Integer sumActiveHoldUserCount(
            @Param("ptsId") Long ptsId,
            @Param("rhDate") LocalDate rhDate,
            @Param("now") LocalDateTime now
    );

    Integer sumActiveHoldUserCountForDisplay(
            @Param("ptsId") Long ptsId,
            @Param("rhDate") LocalDate rhDate,
            @Param("now") LocalDateTime now
    );

    void insert(ReservationHold hold);

    void updateStatusToUsed(@Param("rhId") Long rhId);
}
