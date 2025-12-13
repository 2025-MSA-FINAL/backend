package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.SlotInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SlotInventoryMapper {

    int deleteByPopId(@Param("popId") Long popId);

    int insertBatch(@Param("list") List<SlotInventory> list);

    /**
     * 남은 좌석이 충분할 때만 remain_capacity 차감
     * 성공: 1 / 실패: 0
     */
    int decreaseIfAvailable(
            @Param("ptsId") Long ptsId,
            @Param("invDate") LocalDate invDate,
            @Param("cnt") int cnt
    );

    /**
     * ✅ 특정 날짜에 대해, 특정 슬롯들(ptsIds)의 inventory를 한 번에 조회
     * (getTimeSlotsByDate에서 N+1 방지)
     */
    List<SlotInventory> findByPtsIdsAndDate(
            @Param("ptsIds") List<Long> ptsIds,
            @Param("invDate") LocalDate invDate
    );
}
