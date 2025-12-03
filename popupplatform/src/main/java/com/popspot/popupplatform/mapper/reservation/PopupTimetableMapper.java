package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupTimetable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PopupTimetableMapper {

    int deleteByPopId(Long popId);

    int insertTimetable(PopupTimetable timetable);

    List<PopupTimetable> findByPopId(Long popId);
}