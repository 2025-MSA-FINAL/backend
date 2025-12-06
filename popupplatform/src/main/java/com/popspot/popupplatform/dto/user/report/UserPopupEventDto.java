package com.popspot.popupplatform.dto.user.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPopupEventDto {

    private Long popId;              // 팝업 ID
    private String eventType;        // VIEW / WISHLIST / RESERVATION
    private LocalDateTime eventAt;   // 이벤트 발생 시각
    private String priceType;        // POPUPSTORE.pop_price_type (FREE / PAID ...)
    private Integer peopleCount;     // 예약 인원 (VIEW/WISHLIST는 null)
    private String region;           // POPUPSTORE.pop_location
    private String hashtags;         // "감성카페,전시" 처럼 ,로 이어진 문자열
}
