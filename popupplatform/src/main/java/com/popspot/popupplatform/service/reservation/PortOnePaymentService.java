package com.popspot.popupplatform.service.reservation;

import java.util.Map;

public interface PortOnePaymentService {
    void handleWebhook(String rawBody, String webhookId, String timestamp, String signature, Long userId);

    /**
     * ✅ 로컬/웹훅 미수신 환경 대응:
     * 프론트에서 결제 성공 후 paymentId를 전달하면
     * 서버가 PortOne REST API로 결제 상태를 검증하고(단건조회),
     * HOLD 기반으로 예약을 확정 처리한다.
     */
    Map<String, Object> completePayment(String paymentId, Long userId);

    void cancelPortOnePayment(String paymentId, String reason);
}
