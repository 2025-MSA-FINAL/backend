package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.reservation.PopupBlock;
import com.popspot.popupplatform.domain.reservation.PopupReservation;
import com.popspot.popupplatform.domain.reservation.PopupTimetable;
import com.popspot.popupplatform.dto.reservation.request.PopupExcludeDateRequest;
import com.popspot.popupplatform.dto.reservation.request.PopupReservationRequest;
import com.popspot.popupplatform.dto.reservation.request.PopupReservationSettingRequest;
import com.popspot.popupplatform.dto.reservation.request.PopupTimetableRequest;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationSettingResponse;
import com.popspot.popupplatform.mapper.reservation.PopupBlockMapper;
import com.popspot.popupplatform.mapper.reservation.PopupReservationMapper;
import com.popspot.popupplatform.mapper.reservation.PopupTimetableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PopupReservationServiceImpl implements PopupReservationService {

    private final PopupReservationMapper popupReservationMapper;
    private final PopupTimetableMapper popupTimetableMapper;
    private final PopupBlockMapper popupBlockMapper;

    /**
     * 예약 설정 저장 (기존 설정 전체 덮어쓰기)
     */
    @Transactional
    @Override
    public PopupReservationSettingResponse saveReservationSetting(Long popId, PopupReservationSettingRequest req) {

        // 1) POPUP_RESERVATION 저장 (upsert)
        PopupReservationRequest info = req.getReservationInfo();

        PopupReservation reservation = new PopupReservation();
        reservation.setPopId(popId);
        reservation.setPrStartTime(info.getStartDate());
        reservation.setPrEndTime(info.getEndDate());
        reservation.setPrMaxUserCnt(info.getMaxUserCnt());
        reservation.setPrEntryTimeUnit(info.getEntryTimeUnit()); // enum

        popupReservationMapper.insertPopupReservation(reservation);

        var baseDate = info.getStartDate().toLocalDate();

        // 2) 기존 타임테이블 삭제 후 다시 저장
        popupTimetableMapper.deleteByPopId(popId);

        if (req.getTimetables() != null) {
            for (PopupTimetableRequest t : req.getTimetables()) {
                PopupTimetable timetable = new PopupTimetable();
                timetable.setPopId(popId);
                timetable.setPtDayOfWeek(t.getDayOfWeek()); // enum
                timetable.setPtCapacity(t.getCapacity());
                timetable.setPtStartDateTime(baseDate.atTime(t.getStartTime()));
                timetable.setPtEndDateTime(baseDate.atTime(t.getEndTime()));

                popupTimetableMapper.insertTimetable(timetable);
            }
        }

        // 3) 기존 제외일 삭제 후 다시 저장
        popupBlockMapper.deleteByPopId(popId);

        if (req.getExcludeDates() != null) {
            for (PopupExcludeDateRequest d : req.getExcludeDates()) {
                PopupBlock block = new PopupBlock();
                block.setPopId(popId);
                block.setPbDateTime(d.getDate().atStartOfDay());
                popupBlockMapper.insertBlock(block);
            }
        }

        // ✅ 최종적으로 DB 기준 최신 상태를 Response DTO로 반환
        return getReservationSetting(popId);
    }

    /**
     * 예약 설정 조회
     */
    @Override
    public PopupReservationSettingResponse getReservationSetting(Long popId) {

        PopupReservation reservation = popupReservationMapper.findByPopId(popId);
        if (reservation == null) {
            return null; // 필요하면 CustomException으로 바꿔도 됨
        }

        List<PopupTimetable> timetables = popupTimetableMapper.findByPopId(popId);
        List<PopupBlock> blocks = popupBlockMapper.findByPopId(popId);

        return PopupReservationSettingResponse.of(reservation, timetables, blocks);
    }
}