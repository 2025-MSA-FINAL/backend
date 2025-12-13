package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.reservation.UserReservation;
import com.popspot.popupplatform.dto.reservation.SlotWithReservationDto;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ReservationErrorCode;
import com.popspot.popupplatform.mapper.reservation.PopupTimeSlotMapper;
import com.popspot.popupplatform.mapper.reservation.SlotInventoryMapper;
import com.popspot.popupplatform.mapper.reservation.UserReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final PopupTimeSlotMapper popupTimeSlotMapper;
    private final UserReservationMapper userReservationMapper;

    // ✅ 추가
    private final SlotInventoryMapper slotInventoryMapper;

    private final AtomicLong testUserId = new AtomicLong(1L);

    // TODO: 실제 로그인 사용자에서 받아오도록 교체
    private Long getCurrentUserId() {
        return testUserId.getAndIncrement();
    }

    /**
     * 결제 없이: (inventory remain 차감) + USER_RESERVATION 즉시 확정
     */
    @Transactional
    public Long createReservationConfirmed(Long popupId,
                                           Long slotId,
                                           String dateStr,
                                           Integer people) {

        LocalDate date = LocalDate.parse(dateStr); // "yyyy-MM-dd"
        Long userId = getCurrentUserId();

        SlotWithReservationDto slot = popupTimeSlotMapper.findSlotWithPopupReservation(slotId);

        // 순서 주의: slot null 먼저 체크
        if (slot == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND);
        }
        if (!slot.getPopId().equals(popupId)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND);
        }
        if (people == null || people <= 0 || slot.getMaxUserCnt() < people) {
            throw new CustomException(ReservationErrorCode.INVALID_PEOPLE_COUNT);
        }

        // ✅ 1) inventory에서 남은 좌석 원자적 차감
        int updated = slotInventoryMapper.decreaseIfAvailable(slotId, date, people);
        if (updated == 0) {
            // 남은 좌석 부족(혹은 inventory row가 없어서 실패)
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_FULL);
        }

        // ✅ 2) 예약 즉시 확정 insert
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
        return reservation.getUrId();
    }
}
