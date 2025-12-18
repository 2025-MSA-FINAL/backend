package com.popspot.popupplatform.controller.reservation;

import com.popspot.popupplatform.service.reservation.PortOnePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "PortOne Payment", description = "PortOne 결제 검증/완료 처리 API")
@RestController
@RequestMapping("/api/payments/portone")
@RequiredArgsConstructor
public class PortOnePaymentController {

    private final PortOnePaymentService portOnePaymentService;

    @Operation(summary = "결제 완료 검증 + 예약 확정 (웹훅 대체용)")
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> complete(@RequestBody CompleteRequest req) {
        Map<String, Object> result = portOnePaymentService.completePayment(req.getPaymentId());
        return ResponseEntity.ok(result);
    }

    @Data
    public static class CompleteRequest {
        private String paymentId;
    }
}
