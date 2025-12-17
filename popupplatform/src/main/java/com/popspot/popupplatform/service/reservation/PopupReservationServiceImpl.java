package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.domain.reservation.*;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PopupReservationServiceImpl implements PopupReservationService {

    private final PopupReservationMapper popupReservationMapper;
    private final PopupTimetableMapper popupTimetableMapper;
    private final PopupBlockMapper popupBlockMapper;
    private final PopupTimeSlotMapper popupTimeSlotMapper;
    private final PopupMapper popupMapper;

    // ✅ Redis (remain 관리)
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis remain key 포맷
     * - "inv:{popId}:{yyyyMMdd}:{ptsId}" -> 남은 좌석(int)
     *
     * 예) inv:10:20250115:123 = 20
     *
     * ⚠️ 여기서는 slotInventory(DB)를 사용하지 않고 Redis를 “실시간 remain 저장소”로 사용한다.
     */
    private String invKey(Long popId, LocalDate date, Long ptsId) {
        String ymd = date.toString().replace("-", "");
        return "inv:" + popId + ":" + ymd + ":" + ptsId;
    }

    /**
     * 예약 설정 저장 (최초 1회만 허용)
     *
     * 변경 포인트:
     * 1) 기존에는 SLOT_INVENTORY 테이블에 전기간 remain을 생성했음 :contentReference[oaicite:2]{index=2}
     * 2) 이제는 “전기간 remain 초기값”을 Redis에 생성한다.
     *
     * ⚠️ Redis 초기화는 DB 트랜잭션 커밋 이후(afterCommit)에 수행해야
     * DB 롤백 시 Redis만 남는 불일치가 생기지 않는다.
     */
    @Transactional
    @Override
    public PopupReservationSettingResponse saveReservationSetting(Long popId, PopupReservationSettingRequest req) {

        Optional<PopupStore> popupStore = popupMapper.selectPopupDetail(popId);
        if (popupStore.isEmpty()) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }
        PopupStore ps = popupStore.get();

        PopupReservation existed = popupReservationMapper.findByPopId(popId);
        if (existed != null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        validateReservationRequest(popId, req);

        PopupReservationRequest reservationReq = req.getReservationInfo();
        PopupReservation reservation = toReservationEntity(popId, reservationReq);
        popupReservationMapper.insertPopupReservation(reservation);

        // 1) 제외일(블락) 저장
        List<PopupBlock> blockEntities = new ArrayList<>();
        if (req.getExcludeDates() != null) {
            for (PopupExcludeDateRequest excludeReq : req.getExcludeDates()) {
                PopupBlock block = toBlockEntity(popId, excludeReq);
                popupBlockMapper.insertBlock(block);
                blockEntities.add(block);
            }
        }

        // 2) 타임테이블 저장 + 타임슬롯 생성(ptsId 생성됨) :contentReference[oaicite:3]{index=3} :contentReference[oaicite:4]{index=4}
        List<PopupTimetable> timetableEntities = new ArrayList<>();
        if (req.getTimetables() != null) {
            for (PopupTimetableRequest ttReq : req.getTimetables()) {
                PopupTimetable timetable = toTimetableEntity(popId, ttReq);
                popupTimetableMapper.insertTimetable(timetable);
                timetableEntities.add(timetable);

                generateTimeSlots(popId, reservation, timetable);
            }
        }

        // ✅ (변경) inventory(DB) 생성 대신 Redis remain 초기화
        // - 커밋 이후(afterCommit)에 Redis SET 해야 안전함
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                initRedisInventory(popId, req, ps);
            }
        });

        popupMapper.updateIsReservation(popId);

        return PopupReservationSettingResponse.of(reservation, timetableEntities, blockEntities);
    }

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
        if (popupStore.isEmpty()) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }
        PopupStore ps = popupStore.get();

        LocalDate startDate = ps.getPopStartDate().toLocalDate();
        LocalDate endDate = ps.getPopEndDate().toLocalDate();

        List<PopupTimetable> timetables = popupTimetableMapper.findByPopId(popId);
        List<PopupBlock> blocks = popupBlockMapper.findByPopId(popId);

        Set<DayOfWeekType> openDays = new HashSet<>();
        for (PopupTimetable tt : timetables) {
            openDays.add(tt.getPtDayOfWeek());
        }

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
    //    ✅ Redis remain 기반 remainingCount 조회
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

        DayOfWeekType targetDow = toDayOfWeekType(date.getDayOfWeek());

        // 요일에 해당하는 슬롯 정의(ptsId 목록) 조회 :contentReference[oaicite:5]{index=5} :contentReference[oaicite:6]{index=6}
        List<PopupTimeSlot> slots = popupTimeSlotMapper.findByPopIdAndDayOfWeek(popId, targetDow);
        if (slots == null || slots.isEmpty()) {
            return PopupTimeSlotListResponse.builder()
                    .date(date)
                    .timeSlots(List.of())
                    .build();
        }

        // ✅ Redis MGET로 remain을 한 번에 가져온다 (DB IN 조회 대체)
        List<String> keys = new ArrayList<>(slots.size());
        for (PopupTimeSlot s : slots) {
            keys.add(invKey(popId, date, s.getPtsId()));
        }

        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);

        // ptsId -> remain map 구성 (values는 keys와 동일한 순서)
        Map<Long, Integer> remainMap = new HashMap<>();
        for (int i = 0; i < slots.size(); i++) {
            PopupTimeSlot s = slots.get(i);
            String v = (values == null ? null : values.get(i));
            int remain = 0;

            // Redis에 키가 없다면:
            // - 아직 초기화가 안 됐거나(설정 직후 레이스),
            // - 이미 기간 종료/정리된 상태일 수 있다.
            // 여기서는 안전하게 0으로 보여준다(예약 불가).
            if (v != null) {
                try {
                    remain = Integer.parseInt(v);
                } catch (NumberFormatException ignore) {
                    remain = 0;
                }
            }

            remainMap.put(s.getPtsId(), remain);
        }

        List<PopupTimeSlotResponse> result = new ArrayList<>();
        for (PopupTimeSlot s : slots) {
            int remaining = remainMap.getOrDefault(s.getPtsId(), 0);

            result.add(
                    PopupTimeSlotResponse.builder()
                            .slotId(s.getPtsId())
                            .startTime(s.getPtsStartTime().toString())
                            .endTime(s.getPtsEndTime().toString())
                            .remainingCount(Math.max(remaining, 0))
                            .build()
            );
        }

        return PopupTimeSlotListResponse.builder()
                .date(date)
                .timeSlots(result)
                .build();
    }

    // ====================================================
    // ✅ Redis inventory 초기화 (예약 설정 시 1회)
    // ====================================================
    private void initRedisInventory(Long popId, PopupReservationSettingRequest req, PopupStore ps) {

        Set<LocalDate> excluded = new HashSet<>();
        if (req.getExcludeDates() != null) {
            for (PopupExcludeDateRequest ex : req.getExcludeDates()) {
                excluded.add(ex.getDate());
            }
        }

        // ✅ openDays(운영 요일) 만들기: timetables 기준
        Set<DayOfWeekType> openDays = new HashSet<>();
        if (req.getTimetables() != null) {
            for (PopupTimetableRequest tt : req.getTimetables()) {
                openDays.add(tt.getDayOfWeek());
            }
        }

        List<PopupTimeSlot> slots = popupTimeSlotMapper.findByPopId(popId);
        if (slots == null || slots.isEmpty()) return;

        LocalDate start = ps.getPopStartDate().toLocalDate();
        LocalDate end = ps.getPopEndDate().toLocalDate();

        Map<String, String> kv = new HashMap<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeekType dow = toDayOfWeekType(d.getDayOfWeek());

            // ✅ 요일이 운영 요일이 아니면 skip (캘린더 로직과 동일)
            if (!openDays.contains(dow)) continue;

            // ✅ 제외일이면 skip
            if (excluded.contains(d)) continue;

            // ✅ 그 날짜 요일에 해당하는 슬롯만 생성
            for (PopupTimeSlot s : slots) {
                if (s.getPtsDayOfWeek() != dow) continue;
                kv.put(invKey(popId, d, s.getPtsId()), String.valueOf(s.getPtsCapacity()));
            }
        }

        if (!kv.isEmpty()) {
            stringRedisTemplate.opsForValue().multiSet(kv);

            // ✅ 예약 만료일(=팝업 종료일) 기준으로 TTL(EXPIREAT) 설정
            // - 종료일 다음날 00:00에 일괄 만료(원하면 plusDays(0)로 “종료일 23:59:59” 느낌도 가능)
            Instant expireAt = ps.getPopEndDate()
                    .toLocalDate()
                    .plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();

            for (String k : kv.keySet()) {
                stringRedisTemplate.expireAt(k, expireAt);
            }
        }
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
    // Validation
    // ========================================================
    private void validateReservationRequest(Long popId, PopupReservationSettingRequest req) {

        if (req.getReservationInfo() == null ||
                req.getReservationInfo().getEntryTimeUnit() == null) {
            throw new CustomException(ReservationErrorCode.INVALID_ENTRY_TIME_UNIT);
        }

        if (req.getTimetables() != null) {

            Set<DayOfWeekType> daySet = new HashSet<>();
            for (PopupTimetableRequest tt : req.getTimetables()) {

                if (!daySet.add(tt.getDayOfWeek())) {
                    throw new CustomException(ReservationErrorCode.DUPLICATE_TIMETABLE_DAY);
                }

                if (tt.getStartTime().compareTo(tt.getEndTime()) >= 0) {
                    throw new CustomException(ReservationErrorCode.INVALID_TIMETABLE_TIME_RANGE);
                }
            }
        }

        if (req.getExcludeDates() != null) {
            for (PopupExcludeDateRequest exclude : req.getExcludeDates()) {
                if (exclude.getDate() == null) {
                    throw new CustomException(ReservationErrorCode.INVALID_EXCLUDE_DATE);
                }
            }
        }
    }

    // ========================================================
    // 기존 변환/슬롯 생성 로직
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

        int cap = timetable.getPtCapacity();

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
