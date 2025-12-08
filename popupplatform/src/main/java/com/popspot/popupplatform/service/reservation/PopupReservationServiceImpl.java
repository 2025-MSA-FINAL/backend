package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.domain.reservation.PopupBlock;
import com.popspot.popupplatform.domain.reservation.PopupReservation;
import com.popspot.popupplatform.domain.reservation.PopupTimeSlot;
import com.popspot.popupplatform.domain.reservation.PopupTimetable;
import com.popspot.popupplatform.dto.reservation.enums.DayOfWeekType;
import com.popspot.popupplatform.dto.reservation.enums.EntryTimeUnit;
import com.popspot.popupplatform.dto.reservation.request.PopupExcludeDateRequest;
import com.popspot.popupplatform.dto.reservation.request.PopupReservationRequest;
import com.popspot.popupplatform.dto.reservation.request.PopupReservationSettingRequest;
import com.popspot.popupplatform.dto.reservation.request.PopupTimetableRequest;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationCalendarResponse;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationSettingResponse;
import com.popspot.popupplatform.dto.reservation.response.PopupTimeSlotListResponse;
import com.popspot.popupplatform.dto.reservation.response.PopupTimeSlotResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.PopupErrorCode;
import com.popspot.popupplatform.global.exception.code.ReservationErrorCode;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.reservation.PopupBlockMapper;
import com.popspot.popupplatform.mapper.reservation.PopupReservationMapper;
import com.popspot.popupplatform.mapper.reservation.PopupTimeSlotMapper;
import com.popspot.popupplatform.mapper.reservation.PopupTimetableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class PopupReservationServiceImpl implements PopupReservationService {

    private final PopupReservationMapper popupReservationMapper;
    private final PopupTimetableMapper popupTimetableMapper;
    private final PopupBlockMapper popupBlockMapper;
    private final PopupTimeSlotMapper popupTimeSlotMapper;
    private final PopupMapper popupMapper;

    /**
     * 예약 설정 저장 (최초 1회만 허용)
     */
    @Transactional
    @Override
    public PopupReservationSettingResponse saveReservationSetting(Long popId, PopupReservationSettingRequest req) {

        // 이미 설정이 있으면 수정 불가
        PopupReservation existed = popupReservationMapper.findByPopId(popId);
        if (existed != null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        // -------------------------
        // 🔥 에러 코드 기반 검증
        // -------------------------
        validateReservationRequest(popId, req);

        // -------------------------
        // 예약 설정 저장
        // -------------------------
        PopupReservationRequest reservationReq = req.getReservationInfo();
        PopupReservation reservation = toReservationEntity(popId, reservationReq);
        popupReservationMapper.insertPopupReservation(reservation);

        List<PopupBlock> blockEntities = new ArrayList<>();
        if (req.getExcludeDates() != null) {
            for (PopupExcludeDateRequest excludeReq : req.getExcludeDates()) {
                PopupBlock block = toBlockEntity(popId, excludeReq);
                popupBlockMapper.insertBlock(block);
                blockEntities.add(block);
            }
        }

        List<PopupTimetable> timetableEntities = new ArrayList<>();
        if (req.getTimetables() != null) {
            for (PopupTimetableRequest ttReq : req.getTimetables()) {
                PopupTimetable timetable = toTimetableEntity(popId, ttReq);
                popupTimetableMapper.insertTimetable(timetable);
                timetableEntities.add(timetable);

                // 요일 시간표 기반으로 실제 슬롯 생성
                generateTimeSlots(popId, reservation, timetable);
            }
        }

        return PopupReservationSettingResponse.of(reservation, timetableEntities, blockEntities);
    }

    /**
     * 조회
     */
    @Transactional(readOnly = true)
    @Override
    public PopupReservationSettingResponse getReservationSetting(Long popId) {

        PopupReservation reservation = popupReservationMapper.findByPopId(popId);
        if (reservation == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        List<PopupTimetable> timetables = popupTimetableMapper.findByPopId(popId);
        List<PopupBlock> blocks = popupBlockMapper.findByPopId(popId);

        return PopupReservationSettingResponse.of(reservation, timetables, blocks);
    }

    // ====================================================
    // 1) 캘린더 응답
    // ====================================================
    @Transactional(readOnly = true)
    @Override
    public PopupReservationCalendarResponse getReservationCalendar(Long popId) {

        PopupReservation reservation = popupReservationMapper.findByPopId(popId);
        if (reservation == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        Optional<PopupStore> popupStore = popupMapper.selectPopupDetail(popId);
        if(popupStore.isEmpty()){
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }
        PopupStore ps = popupStore.get();

        LocalDate startDate = ps.getPopStartDate().toLocalDate();
        LocalDate endDate = ps.getPopEndDate().toLocalDate();

        List<PopupTimetable> timetables = popupTimetableMapper.findByPopId(popId);
        List<PopupBlock> blocks = popupBlockMapper.findByPopId(popId);

        // 운영 요일 Set
        Set<DayOfWeekType> openDays = new HashSet<>();
        for (PopupTimetable tt : timetables) {
            openDays.add(tt.getPtDayOfWeek());
        }

        // 제외일 Set
        Set<LocalDate> blockedDates = new HashSet<>();
        for (PopupBlock b : blocks) {
            blockedDates.add(b.getPbDateTime().toLocalDate());
        }

        List<LocalDate> availableDates = new ArrayList<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DayOfWeekType dowType = toDayOfWeekType(d.getDayOfWeek());
            if (!openDays.contains(dowType)) continue;
            if (blockedDates.contains(d)) continue;
            availableDates.add(d);
        }

        return PopupReservationCalendarResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .availableDates(availableDates)
                .build();
    }

    // ====================================================
// 2) 특정 날짜 슬롯 목록
//    ✅ 이제는 POPUP_TIME_SLOT(pts_id) 기반으로 응답
// ====================================================
    @Transactional(readOnly = true)
    @Override
    public PopupTimeSlotListResponse getTimeSlotsByDate(Long popId, LocalDate date) {

        PopupReservation reservation = popupReservationMapper.findByPopId(popId);
        if (reservation == null) {
            return PopupTimeSlotListResponse.builder()
                    .date(date)
                    .timeSlots(List.of())
                    .build();
        }

        // 요일 구해서 해당 요일의 슬롯 템플릿 조회
        DayOfWeekType targetDow = toDayOfWeekType(date.getDayOfWeek());

        // ✅ POPUP_TIME_SLOT에서 직접 조회
        List<PopupTimeSlot> slots =
                popupTimeSlotMapper.findByPopIdAndDayOfWeek(popId, targetDow);

        if (slots == null || slots.isEmpty()) {
            return PopupTimeSlotListResponse.builder()
                    .date(date)
                    .timeSlots(List.of())
                    .build();
        }

        // TODO: 여기서 나중에 실제 예약/홀드 인원 빼서 remainingCount 계산
        List<PopupTimeSlotResponse> result = new ArrayList<>();
        for (PopupTimeSlot s : slots) {
            result.add(
                    PopupTimeSlotResponse.builder()
                            .slotId(s.getPtsId())
                            .startTime(s.getPtsStartTime().toString())
                            .endTime(s.getPtsEndTime().toString())
                            .remainingCount(s.getPtsCapacity()) // 일단 capacity 그대로
                            .build()
            );
        }

        return PopupTimeSlotListResponse.builder()
                .date(date)
                .timeSlots(result)
                .build();
    }

    // ====================================================
    // DayOfWeek → DayOfWeekType 변환 헬퍼
    // ====================================================
    private DayOfWeekType toDayOfWeekType(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DayOfWeekType.MON;
            case TUESDAY -> DayOfWeekType.TUE;
            case WEDNESDAY -> DayOfWeekType.WED;
            case THURSDAY -> DayOfWeekType.THU;
            case FRIDAY -> DayOfWeekType.FRI;
            case SATURDAY -> DayOfWeekType.SAT;
            case SUNDAY -> DayOfWeekType.SUN;
        };
    }


    // ========================================================
    // 🔥 여기부터 검증(Validation) 메서드 — 기존 로직 손대지 않음
    // ========================================================

    private void validateReservationRequest(Long popId, PopupReservationSettingRequest req) {

        // (1) 예약 단위 시간 검증
        if (req.getReservationInfo() == null ||
                req.getReservationInfo().getEntryTimeUnit() == null) {
            throw new CustomException(ReservationErrorCode.INVALID_ENTRY_TIME_UNIT);
        }

        // (2) 시간표 검증
        if (req.getTimetables() != null) {

            // 중복 요일 체크
            Set<DayOfWeekType> daySet = new HashSet<>();
            for (PopupTimetableRequest tt : req.getTimetables()) {

                // 요일 중복 검증
                if (!daySet.add(tt.getDayOfWeek())) {
                    throw new CustomException(ReservationErrorCode.DUPLICATE_TIMETABLE_DAY);
                }

                // 시간 범위 검증
                if (tt.getStartTime().compareTo(tt.getEndTime()) >= 0) {
                    throw new CustomException(ReservationErrorCode.INVALID_TIMETABLE_TIME_RANGE);
                }
            }
        }

        // (3) 제외일 검증 (null, 과거 날짜 등은 정책 맞춰서 적용)
        if (req.getExcludeDates() != null) {
            for (PopupExcludeDateRequest exclude : req.getExcludeDates()) {
                if (exclude.getDate() == null) {
                    throw new CustomException(ReservationErrorCode.INVALID_EXCLUDE_DATE);
                }
            }
        }
    }

    // ========================================================
    // 아래부터는 기존 로직 그대로 — 절대 손댄 것 없음
    // ========================================================

    private PopupReservation toReservationEntity(Long popId, PopupReservationRequest req) {
        PopupReservation r = new PopupReservation();
        r.setPopId(popId);
        r.setPrEntryTimeUnit(req.getEntryTimeUnit());
        r.setPrStartTime(req.getStartDate());
        r.setPrEndTime(req.getEndDate());
        r.setPrMaxUserCnt(req.getMaxUserCnt());
        return r;
    }

    private PopupBlock toBlockEntity(Long popId, PopupExcludeDateRequest req) {
        PopupBlock b = new PopupBlock();
        b.setPopId(popId);
        b.setPbDateTime(req.getDate().atStartOfDay());
        return b;
    }

    private PopupTimetable toTimetableEntity(Long popId, PopupTimetableRequest req) {
        PopupTimetable t = new PopupTimetable();
        t.setPopId(popId);
        t.setPtDayOfWeek(req.getDayOfWeek());

        LocalDate dummy = LocalDate.of(1970, 1, 1);
        t.setPtStartDateTime(LocalDateTime.of(dummy, req.getStartTime()));
        t.setPtEndDateTime(LocalDateTime.of(dummy, req.getEndTime()));
        t.setPtCapacity(req.getCapacity());
        return t;
    }

    private void generateTimeSlots(Long popId, PopupReservation reservation, PopupTimetable timetable) {
        EntryTimeUnit unit = reservation.getPrEntryTimeUnit();
        LocalTime start = timetable.getPtStartDateTime().toLocalTime();
        LocalTime end = timetable.getPtEndDateTime().toLocalTime();

        int cap = timetable.getPtCapacity() != null
                ? timetable.getPtCapacity()
                : reservation.getPrMaxUserCnt();

        if (unit == EntryTimeUnit.ALL_DAY) {
            PopupTimeSlot slot = new PopupTimeSlot();
            slot.setPopId(popId);
            slot.setPtsDayOfWeek(timetable.getPtDayOfWeek());
            slot.setPtsStartTime(start);
            slot.setPtsEndTime(end);
            slot.setPtsCapacity(cap);
            popupTimeSlotMapper.insertTimeSlot(slot);
            return;
        }

        int minutes = (unit == EntryTimeUnit.MIN30 ? 30 : 60);

        LocalTime cursor = start;
        while (cursor.isBefore(end)) {
            LocalTime next = cursor.plusMinutes(minutes);
            if (next.isAfter(end)) break;

            PopupTimeSlot slot = new PopupTimeSlot();
            slot.setPopId(popId);
            slot.setPtsDayOfWeek(timetable.getPtDayOfWeek());
            slot.setPtsStartTime(cursor);
            slot.setPtsEndTime(next);
            slot.setPtsCapacity(cap);
            popupTimeSlotMapper.insertTimeSlot(slot);

            cursor = next;
        }
    }
}