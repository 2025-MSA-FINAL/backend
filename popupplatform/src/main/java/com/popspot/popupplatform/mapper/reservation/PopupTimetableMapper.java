package com.popspot.popupplatform.mapper.reservation;

import com.popspot.popupplatform.domain.reservation.PopupTimetable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PopupTimetableMapper {

    /**
     * 해당 팝업의 기존 요일별 시간표 모두 삭제
     */
    int deleteByPopId(Long popId);

    /**
     * 요일별 시간표 1건 추가
     */
    int insertTimetable(PopupTimetable timetable);

    /**
     * 팝업의 요일별 시간표 전체 조회
     */
    List<PopupTimetable> findByPopId(Long popId);
}
