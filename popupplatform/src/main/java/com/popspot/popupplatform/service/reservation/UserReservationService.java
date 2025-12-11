package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.reservation.PopupReservation;
import com.popspot.popupplatform.domain.reservation.PopupTimeSlot;
import com.popspot.popupplatform.domain.reservation.ReservationHold;
import com.popspot.popupplatform.domain.reservation.UserReservation;
import com.popspot.popupplatform.dto.reservation.SlotWithReservationDto;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ReservationErrorCode;
import com.popspot.popupplatform.mapper.reservation.PopupReservationMapper;
import com.popspot.popupplatform.mapper.reservation.PopupTimeSlotMapper;
import com.popspot.popupplatform.mapper.reservation.ReservationHoldMapper;
import com.popspot.popupplatform.mapper.reservation.UserReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final PopupTimeSlotMapper popupTimeSlotMapper;
    private final UserReservationMapper userReservationMapper;
    private final ReservationHoldMapper reservationHoldMapper;
    private final PopupReservationMapper popupReservationMapper;

    private final AtomicLong testUserId = new AtomicLong(1L);

    // TODO: 실제 로그인 사용자에서 받아오도록 교체
    private Long getCurrentUserId() {
        return testUserId.getAndIncrement();
    }

    /**
     * 결제 없이: 좌석 체크 + HOLD + USER_RESERVATION 즉시 확정
     */
    @Transactional
    public Long createReservationWithHold(Long popupId,
                                          Long slotId,
                                          String dateStr,
                                          Integer people) {

        LocalDate date = LocalDate.parse(dateStr); // "yyyy-MM-dd"
        Long userId = getCurrentUserId();

        SlotWithReservationDto slot = popupTimeSlotMapper.findSlotWithPopupReservation(slotId);

        if (people == null || people <= 0 || slot.getMaxUserCnt()<people) {
            throw new CustomException(ReservationErrorCode.INVALID_PEOPLE_COUNT);
        }
        if (slot == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND);
        }
        if (!slot.getPopId().equals(popupId)) {
            // 팝업-슬롯 불일치 시에도 같은 에러 처리
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND);
        }

        int capacity = slot.getCapacity();

        // 2) 해당 날짜의 확정 예약 인원 합 (USER_RESERVATION)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Integer confirmed = userReservationMapper.sumConfirmedUserCount(
                slotId, startOfDay, endOfDay
        );
        if (confirmed == null) confirmed = 0;

        // 3) 해당 날짜의 ACTIVE HOLD 인원 합
        Integer holding = reservationHoldMapper.sumActiveHoldUserCount(
                slotId, date, LocalDateTime.now()
        );
        if (holding == null) holding = 0;

        int available = capacity - confirmed - holding;
        if (available < people) {
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_FULL);
        }

        // 4) HOLD 생성 (ACTIVE) - 지금은 바로 USED로 바꿀 예정
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(2);

        ReservationHold hold = ReservationHold.builder()
                .ptsId(slotId)
                .userId(userId)
                .rhDate(date)
                .rhUserCnt(people)
                .rhStatus("ACTIVE")
                .rhExpiresAt(expiresAt)
                .build();

        reservationHoldMapper.insert(hold);
        Long holdId = hold.getRhId();

        // 5) USER_RESERVATION 즉시 확정
        LocalDateTime visitDateTime = LocalDateTime.of(date, slot.getStartTime());

        UserReservation reservation = UserReservation.builder()
                .popId(popupId)
                .ptsId(slotId)
                .userId(userId)
                .urDateTime(visitDateTime)
                .urUserCnt(people)
                .urStatus(true)
                .build();

        userReservationMapper.insert(reservation);
        Long reservationId = reservation.getUrId();

        // 6) HOLD 사용 처리 (나중에 결제 붙이면 이 부분을 결제 성공 시점으로 이동)
        reservationHoldMapper.updateStatusToUsed(holdId);

        return reservationId;
    }
}
