package com.popspot.popupplatform.service.reservation;

import java.time.LocalDate;
import java.util.Map;

public interface UserReservationService {
    Long createReservationConfirmed(Long popupId, Long slotId, LocalDate date, int people);

    Map<String, Object> createReservationHold(Long popupId, Long slotId, LocalDate date, int people);
}
