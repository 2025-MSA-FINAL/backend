package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupTimeSlot;
import com.popspot.popupplatform.dto.reservation.SlotWithReservationDto;
import com.popspot.popupplatform.dto.reservation.enums.DayOfWeekType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PopupTimeSlotMapper {

    int deleteByPopId(Long popId);

    int insertTimeSlot(PopupTimeSlot slot);

    List<PopupTimeSlot> findByPopId(Long popId);

    List<PopupTimeSlot> findByPopIdAndDayOfWeek(
            @Param("popId") Long popId,
            @Param("dayOfWeek") DayOfWeekType dayOfWeek
    );
    PopupTimeSlot findById(@Param("ptsId") Long ptsId);

    /**
     * 팝업 슬롯 정보 + POPUP_RESERVATION.pr_max_user_cnt JOIN 조회
     */
    SlotWithReservationDto findSlotWithPopupReservation(@Param("slotId") Long slotId);
}
