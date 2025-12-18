package com.popspot.popupplatform.controller.reservation;

import com.popspot.popupplatform.service.reservation.PortOnePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/portone")
@RequiredArgsConstructor
public class PortOneWebhookController {

    private final PortOnePaymentService portOnePaymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String rawBody,
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String timestamp,
            @RequestHeader("webhook-signature") String signature
    ) {
        portOnePaymentService.handleWebhook(rawBody, webhookId, timestamp, signature);
        return ResponseEntity.ok().build();
    }
}
