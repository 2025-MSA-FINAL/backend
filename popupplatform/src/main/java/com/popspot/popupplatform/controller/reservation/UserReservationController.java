package com.popspot.popupplatform.controller.reservation;

import com.popspot.popupplatform.dto.reservation.request.UserReservationCreateRequest;
import com.popspot.popupplatform.service.reservation.UserReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "User Reservation", description = "사용자 예약 API")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class UserReservationController {

    private final UserReservationService userReservationService;

    @Operation(summary = "예약 생성 (결제 없음, HOLD + 즉시 확정)")
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody UserReservationCreateRequest request
    ) {

        Long reservationId = userReservationService.createReservationWithHold(
                request.getPopupId(),
                request.getSlotId(),
                request.getDate(),
                request.getPeople()
        );

        return ResponseEntity.ok(Map.of(
                "reservationId", reservationId
        ));
    }
}