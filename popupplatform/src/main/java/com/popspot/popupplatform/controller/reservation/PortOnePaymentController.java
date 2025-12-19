package com.popspot.popupplatform.controller.reservation;

import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.service.reservation.PortOnePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<Map<String, Object>> complete(@RequestBody CompleteRequest req,
                                                        @AuthenticationPrincipal UserDetails userDetails)
    {
        if (userDetails == null) {
            throw new CustomException(AuthErrorCode.NO_AUTH_TOKEN);
        }

        // 2. userId 추출 (지금 구조에서는 username = userId 문자열)
        Long userId = Long.parseLong(userDetails.getUsername());
        Map<String, Object> result = portOnePaymentService.completePayment(req.getPaymentId(),userId);
        return ResponseEntity.ok(result);
    }

    @Data
    public static class CompleteRequest {
        private String paymentId;
    }
}
