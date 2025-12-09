package com.popspot.popupplatform.global.geo;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    private final KakaoLocalProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Kakao 주소 검색 API를 이용해 주소 문자열 -> 좌표(위/경도) 조회
     */
    public Optional<GeoPoint> geocodeAddress(String address) {
        if (!StringUtils.hasText(address)) {
            log.warn("[KakaoLocal] 빈 주소 입력, 지오코딩 스킵");
            return Optional.empty();
        }

        // 1) URI로 빌드 + 인코딩
        URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getBaseUrl() + "/v2/local/search/address.json")
                .queryParam("query", address)
                .build()
                .encode()
                .toUri();

        log.info("[KakaoLocal] 주소 지오코딩 요청 시작 - address='{}'", address);
        log.info("[KakaoLocal] 설정값 - baseUrl='{}', hasKey={}",
                properties.getBaseUrl(),
                properties.getRestApiKey() != null && !properties.getRestApiKey().isBlank());
        log.info("[KakaoLocal] 호출 URI 객체 = {}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + properties.getRestApiKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoAddressSearchResponse> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoAddressSearchResponse.class);

            log.info("[KakaoLocal] 응답 status = {}", response.getStatusCode());

            KakaoAddressSearchResponse body = response.getBody();
            log.info("[KakaoLocal] 응답 body = {}", body);

            if (body == null || body.getDocuments() == null || body.getDocuments().isEmpty()) {
                log.warn("[KakaoLocal] 검색 결과 없음 - address='{}'", address);
                return Optional.empty();
            }

            KakaoAddressSearchResponse.Document doc = body.getDocuments().get(0);

            Double longitude = parseDoubleSafe(doc.getX()); // x: 경도
            Double latitude  = parseDoubleSafe(doc.getY()); // y: 위도

            if (latitude == null || longitude == null) {
                log.warn("[KakaoLocal] invalid lat/lng for address='{}', x={}, y={}", address, doc.getX(), doc.getY());
                return Optional.empty();
            }

            log.info("[KakaoLocal] 지오코딩 성공 - address='{}', lat={}, lng={}", address, latitude, longitude);
            return Optional.of(new GeoPoint(latitude, longitude));

        } catch (Exception e) {
            log.warn("[KakaoLocal] geocodeAddress error. address='{}'", address, e);
            return Optional.empty();
        }
    }

    private Double parseDoubleSafe(String value) {
        try {
            return value != null ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            log.warn("[KakaoLocal] Double 파싱 실패 - value='{}'", value);
            return null;
        }
    }

    @Getter
    @Setter
    public static class KakaoAddressSearchResponse {
        private List<Document> documents;

        @Getter
        @Setter
        public static class Document {
            private String x; //경도
            private String y; //위도

            @Override
            public String toString() {
                return "Document{x='" + x + "', y='" + y + "'}";
            }
        }

        @Override
        public String toString() {
            return "KakaoAddressSearchResponse{documents=" + documents + "}";
        }
    }
}
