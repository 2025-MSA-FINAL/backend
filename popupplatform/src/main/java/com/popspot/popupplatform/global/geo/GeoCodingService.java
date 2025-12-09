package com.popspot.popupplatform.global.geo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeoCodingService {

    private final KakaoLocalClient kakaoLocalClient;

    /**
     * 주소 문자열 -> 좌표(Optional)
     */
    public Optional<GeoPoint> findCoordinates(String address) {
        return kakaoLocalClient.geocodeAddress(address);
    }
}
