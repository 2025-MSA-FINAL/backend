package com.popspot.popupplatform.global.geo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeoPoint {
    private final Double latitude;   //위도 (y)
    private final Double longitude;  //경도 (x)
}
