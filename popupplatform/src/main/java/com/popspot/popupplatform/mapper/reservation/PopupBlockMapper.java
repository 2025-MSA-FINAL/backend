package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupBlock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PopupBlockMapper {

    int deleteByPopId(Long popId);

    int insertBlock(PopupBlock block);

    List<PopupBlock> findByPopId(Long popId);
}
