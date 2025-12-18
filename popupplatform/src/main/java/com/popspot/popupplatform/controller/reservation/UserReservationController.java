package com.popspot.popupplatform.controller.reservation;

import com.popspot.popupplatform.dto.reservation.request.UserReservationCreateRequest;
import com.popspot.popupplatform.service.reservation.UserReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User Reservation", description = "사용자 예약 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserReservationController {

    private final UserReservationService userReservationService;

    @Operation(summary = "사용자 예약 생성 (결제 없이 즉시 확정 / inventory 차감)")
    @PostMapping("/reservations")
    public ResponseEntity<Map<String, Object>> createReservation(
            @RequestBody UserReservationCreateRequest request
    ) {
        Long reservationId = userReservationService.createReservationConfirmed(
                request.getPopupId(),
                request.getSlotId(),
                request.getDate(),
                request.getPeople()
        );

        return ResponseEntity.ok(Map.of("reservationId", reservationId));
    }

    @Operation(summary = "결제 연동용 HOLD 생성 (Redis-only / inventory 차감)")
    @PostMapping("/reservations/hold")
    public ResponseEntity<Map<String, Object>> createHold(
            @RequestBody UserReservationCreateRequest request
    ) {
        Map<String, Object> hold = userReservationService.createReservationHold(
                request.getPopupId(),
                request.getSlotId(),
                request.getDate(),
                request.getPeople()
        );
        return ResponseEntity.ok(hold);
    }
}
