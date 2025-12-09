package com.popspot.popupplatform.dto.popup.response;

import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 주변 팝업 카드 응답")
public class PopupNearbyItemResponse {

    @Schema(description = "팝업 ID")
    private Long popId;

    @Schema(description = "팝업 이름")
    private String popName;

    @Schema(description = "썸네일 URL")
    private String popThumbnail;

    @Schema(description = "주소")
    private String popLocation;

    @Schema(description = "위도")
    private Double popLatitude;

    @Schema(description = "경도")
    private Double popLongitude;

    @Schema(description = "가격 타입")
    private PopupPriceType popPriceType;

    @Schema(description = "가격")
    private Integer popPrice;

    @Schema(description = "팝업 상태")
    private PopupStatus popStatus;

    @Schema(description = "현재 위치와의 거리 (km)")
    private Double distanceKm;

    @Schema(description = "찜 여부")
    @Setter
    private Boolean isLiked;
}
