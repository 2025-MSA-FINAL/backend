package com.popspot.popupplatform.service.reservation;

import java.time.LocalDate;
import java.util.Map;

public interface UserReservationService {
    Long createReservationConfirmed(Long popupId, Long slotId, LocalDate date, int people, Long userId);

    Map<String, Object> createReservationHold(Long popupId, Long slotId, LocalDate date, int people, Long userId);

    // ✅ 결제 연동 확정 전용(hold에서 이미 차감됨): Redis 차감 없이 DB insert만
    Long createReservationConfirmedFromHold(Long popupId, Long slotId, LocalDate date, int people, Long userId);

    void cancelReservation(Long reservationId, Long userId);
}
