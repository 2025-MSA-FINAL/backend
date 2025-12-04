package com.popspot.popupplatform.dto.popup.request;

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
@Schema(description = "매니저 팝업 기본 정보 수정 요청 DTO")
public class ManagerPopupUpdateRequest {

    @Schema(description = "팝업 이름")
    private String popName;

    @Schema(description = "팝업 설명")
    private String popDescription;

    @Schema(description = "썸네일 이미지 URL")
    private String popThumbnail;

    @Schema(description = "장소 (주소)")
    private String popLocation;

    @Schema(description = "시작일", example = "2025-12-01T10:00:00")
    private LocalDateTime popStartDate;

    @Schema(description = "종료일", example = "2025-12-05T20:00:00")
    private LocalDateTime popEndDate;

    @Schema(description = "입장료 타입 (FREE / PAID)")
    private String popPriceType;

    @Schema(description = "입장료 (무료면 0)")
    private Integer popPrice;

    @Schema(description = "인스타 URL")
    private String popInstaUrl;

    @Schema(description = "팝업 이미지 URL 리스트 (순서대로 저장됨)")
    private List<String> popImages;

    @Schema(description = "해시태그 리스트 (예: ['힙한', '성수동'])")
    private List<String> hashtags;
}