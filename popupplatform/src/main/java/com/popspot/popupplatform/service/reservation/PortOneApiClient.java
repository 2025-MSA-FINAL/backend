package com.popspot.popupplatform.service.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
public class PortOneApiClient {

    private static final String PORTONE_V2_BASE = "https://api.portone.io";

    // ✅ HttpClient는 스프링 빈 주입 말고 내부에서 생성
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // ✅ ObjectMapper는 스프링 빈으로 주입
    private final ObjectMapper objectMapper;

    @Value("${portone.api-secret:}")
    private String portOneApiSecret;

    public PortOneApiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode fetchPayment(String paymentId) {
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

    public void cancelPayment(String paymentId, String reason) {
        if (portOneApiSecret == null || portOneApiSecret.isBlank()) {
            throw new IllegalStateException("portone.api-secret is missing");
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of("reason", reason));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(PORTONE_V2_BASE + "/payments/" + paymentId + "/cancel"))
                    .header("Authorization", "PortOne " + portOneApiSecret)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            // ✅ 이미 취소된 결제 등으로 409가 올 수 있음: 멱등 처리
            if (res.statusCode() == 409) return;

            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new IllegalStateException(
                        "PortOne POST /payments/{paymentId}/cancel failed. status=" + res.statusCode() + ", body=" + res.body()
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to cancel PortOne payment", e);
        }
    }
}
