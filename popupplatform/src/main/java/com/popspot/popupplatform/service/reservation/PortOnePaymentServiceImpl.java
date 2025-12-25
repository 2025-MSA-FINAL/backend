package com.popspot.popupplatform.service.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.popspot.popupplatform.mapper.reservation.ReservationPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortOnePaymentServiceImpl implements PortOnePaymentService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReservationPaymentMapper reservationPaymentMapper;

    // ✅ 기존 예약 확정 서비스 연결
    private final UserReservationService userReservationService;

    // ✅ PortOne 호출 분리(순환참조 해결에 직접적 영향은 없지만 중복 제거/일관성)
    private final PortOneApiClient portOneApiClient;

    @Override
    public void handleWebhook(String rawBody, String webhookId, String timestamp, String signature, Long userId) {
        // (권장) webhook signature 검증 자리

        String paymentId = extractPaymentId(rawBody);
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId missing");
        }

        completePayment(paymentId, userId);
    }

    @Override
    @Transactional
    public Map<String, Object> completePayment(String paymentId, Long userId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId missing");
        }

        Integer expectedAmount = reservationPaymentMapper.selectAmountByMerchantUid(paymentId);
        if (expectedAmount == null) {
            throw new IllegalStateException("RESERVATION_PAYMENT row not found for paymentId=" + paymentId);
        }

        // ✅ PortOne 결제 조회 (기존 fetchPortOnePayment 대체)
        JsonNode payment = portOneApiClient.fetchPayment(paymentId);

        String status = payment.path("status").asText(null);
        long paidAmount = payment.path("amount").path("total").asLong(-1);

        if (!"PAID".equalsIgnoreCase(status)) {
            reservationPaymentMapper.markFailed(paymentId);
            return Map.of(
                    "paymentId", paymentId,
                    "status", status == null ? "UNKNOWN" : status
            );
        }

        if (paidAmount < 0 || (int) paidAmount != expectedAmount) {
            reservationPaymentMapper.markFailed(paymentId);
            throw new IllegalStateException("Amount mismatch. expected=" + expectedAmount + ", paid=" + paidAmount);
        }

        reservationPaymentMapper.markPaid(paymentId);

        String holdId = paymentId.startsWith("hold-") ? paymentId.substring("hold-".length()) : null;
        if (holdId == null || holdId.isBlank()) {
            reservationPaymentMapper.markFailed(paymentId);
            throw new IllegalStateException("holdId parse failed");
        }

        String holdKey = "hold:" + holdId;
        Map<Object, Object> hold = stringRedisTemplate.opsForHash().entries(holdKey);
        if (hold == null || hold.isEmpty()) {
            reservationPaymentMapper.markFailed(paymentId);
            throw new IllegalStateException("HOLD not found or expired");
        }

        Long reservationId = confirmReservationFromHold(hold, userId);

        // ✅ paymentId == merchantUid(현재 구조) 이므로 paymentId로 업데이트 가능
        reservationPaymentMapper.updateReservationId(paymentId, reservationId);

        // ✅ HOLD 정리
        stringRedisTemplate.delete(holdKey);
        // ✅ 스케줄러 원복 방지용 정리(있다면 같이 삭제)
        stringRedisTemplate.delete("holdmeta:" + holdId);
        stringRedisTemplate.opsForZSet().remove("hold:expiry", holdId);

        return Map.of(
                "paymentId", paymentId,
                "status", "PAID",
                "reservationId", reservationId
        );
    }

    private String extractPaymentId(String rawBody) {
        // ✅ 기존 로직 유지 (PortOneApiClient로 옮기지 않음: 관련 없는 부분 건드리지 않기)
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = om.readTree(rawBody);

            com.fasterxml.jackson.databind.JsonNode n1 = root.at("/data/paymentId");
            if (!n1.isMissingNode() && !n1.isNull()) return n1.asText();

            com.fasterxml.jackson.databind.JsonNode n2 = root.at("/data/payment_id");
            if (!n2.isMissingNode() && !n2.isNull()) return n2.asText();

            com.fasterxml.jackson.databind.JsonNode n3 = root.get("paymentId");
            if (n3 != null && !n3.isNull()) return n3.asText();

            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook body", e);
        }
    }

    private Long confirmReservationFromHold(Map<Object, Object> hold, Long userId) {
        Long popId = Long.parseLong(String.valueOf(hold.get("popId")));
        Long slotId = Long.parseLong(String.valueOf(hold.get("ptsId")));
        LocalDate date = LocalDate.parse(String.valueOf(hold.get("date")));
        int people = Integer.parseInt(String.valueOf(hold.get("people")));

        // ✅ 결제 연동에서는 HOLD에서 이미 차감했으니, 추가 차감 없는 확정만 수행
        return userReservationService.createReservationConfirmedFromHold(popId, slotId, date, people, userId);
    }

    @Override
    public void cancelPortOnePayment(String paymentId, String reason) {
        // ✅ PortOne 실제 결제 취소는 PortOneApiClient가 담당
        portOneApiClient.cancelPayment(paymentId, reason);
    }
}
