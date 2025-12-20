package com.popspot.popupplatform.service.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.mapper.reservation.ReservationPaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortOnePaymentServiceImpl implements PortOnePaymentService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final StringRedisTemplate stringRedisTemplate;
    private final ReservationPaymentMapper reservationPaymentMapper;

    // ✅ 기존 예약 확정 서비스 연결
    private final UserReservationService userReservationService;

    @Value("${portone.api-secret:}")
    private String portOneApiSecret;

    private static final String PORTONE_V2_BASE = "https://api.portone.io";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void handleWebhook(String rawBody, String webhookId, String timestamp, String signature,Long userId) {
        // (권장) webhook signature 검증 자리

        String paymentId = extractPaymentId(rawBody);
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId missing");
        }

        completePayment(paymentId,userId);
    }

    @Override
    public Map<String, Object> completePayment(String paymentId,Long userId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId missing");
        }

        Integer expectedAmount = reservationPaymentMapper.selectAmountByMerchantUid(paymentId);
        if (expectedAmount == null) {
            throw new IllegalStateException("RESERVATION_PAYMENT row not found for paymentId=" + paymentId);
        }

        JsonNode payment = fetchPortOnePayment(paymentId);

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

        Long reservationId = confirmReservationFromHold(hold,userId);

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

    private JsonNode fetchPortOnePayment(String paymentId) {
        if (portOneApiSecret == null || portOneApiSecret.isBlank()) {
            throw new IllegalStateException("portone.api-secret is missing");
        }

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(PORTONE_V2_BASE + "/payments/" + paymentId))
                    .header("Authorization", "PortOne " + portOneApiSecret)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new IllegalStateException(
                        "PortOne GET /payments/{paymentId} failed. status=" + res.statusCode() + ", body=" + res.body()
                );
            }

            JsonNode root = objectMapper.readTree(res.body());
            return root.has("payment") ? root.get("payment") : root;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch PortOne payment", e);
        }
    }

    private String extractPaymentId(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);

            JsonNode n1 = root.at("/data/paymentId");
            if (!n1.isMissingNode() && !n1.isNull()) return n1.asText();

            JsonNode n2 = root.at("/data/payment_id");
            if (!n2.isMissingNode() && !n2.isNull()) return n2.asText();

            JsonNode n3 = root.get("paymentId");
            if (n3 != null && !n3.isNull()) return n3.asText();

            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook body", e);
        }
    }

    private Long confirmReservationFromHold(Map<Object, Object> hold,Long userId) {
        Long popId = Long.parseLong(String.valueOf(hold.get("popId")));
        Long slotId = Long.parseLong(String.valueOf(hold.get("ptsId")));
        LocalDate date = LocalDate.parse(String.valueOf(hold.get("date")));
        int people = Integer.parseInt(String.valueOf(hold.get("people")));

        // ✅ 결제 연동에서는 HOLD에서 이미 차감했으니, 추가 차감 없는 확정만 수행
        return ((UserReservationServiceImpl) userReservationService)
                .createReservationConfirmedFromHold(popId, slotId, date, people,userId);
    }
}
