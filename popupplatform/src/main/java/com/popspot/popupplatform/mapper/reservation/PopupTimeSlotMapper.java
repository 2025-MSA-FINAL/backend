package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupTimeSlot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PopupTimeSlotMapper {

    int deleteByPopId(Long popId);

    int insertTimeSlot(PopupTimeSlot slot);

    List<PopupTimeSlot> findByPopId(Long popId);
}
