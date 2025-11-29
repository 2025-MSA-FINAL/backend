package com.popspot.popupplatform.dto.popup.response;

import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopupListItemResponse {

    @Schema(description = "팝업 ID", example = "105")
    private Long popId;

    @Schema(description = "팝업 이름", example = "팝업 제목")
    private String popName;

    @Schema(description = "팝업 썸네일 이미지 URL", example = "https://example.com/images/thumb.jpg")
    private String popThumbnail;

    @Schema(description = "팝업 위치(주소/지역명)", example = "서울시 성동구")
    private String popLocation;

    @Schema(description = "팝업 시작 일시")
    private LocalDateTime popStartDate;

    @Schema(description = "팝업 종료 일시")
    private LocalDateTime popEndDate;

    @Schema(description = "팝업 진행 상태", example = "ONGOING")
    private PopupStatus popStatus;

    @Schema(description = "가격 타입 (무료/유료)", example = "FREE")
    private PopupPriceType popPriceType;

    @Schema(description = "가격 (무료일 경우 0 또는 null)", example = "0")
    private Integer popPrice;

    @Schema(description = "조회수", example = "123")
    private Long popViewCount;

    @Schema(description = "해시태그 목록", example = "[\"무료\", \"서울\", \"굿즈\"]")
    private List<String> hashtags;

    @Schema(description = "로그인 유저 기준 찜 여부 (로그인 안 되어 있으면 null 또는 false)", example = "false")
    private Boolean isLiked;
}
