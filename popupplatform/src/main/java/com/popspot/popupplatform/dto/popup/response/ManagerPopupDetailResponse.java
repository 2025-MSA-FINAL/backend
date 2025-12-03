package com.popspot.popupplatform.dto.popup.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저용 팝업 상세 정보 응답")
public class ManagerPopupDetailResponse {

    @Schema(description = "팝업 ID")
    private Long popId;

    @Schema(description = "팝업 이름")
    private String popName;

    @Schema(description = "썸네일 이미지 URL")
    private String popThumbnail;

    @Schema(description = "AI 요약 멘트")
    private String popAiSummary;

    @Schema(description = "장소 (주소)")
    private String popLocation;

    @Schema(description = "시작일")
    private LocalDateTime popStartDate;

    @Schema(description = "종료일")
    private LocalDateTime popEndDate;

    @Schema(description = "입장료 (무료/유료)")
    private String popPriceType; // FREE or PAID

    @Schema(description = "가격")
    private Integer popPrice;

    @Schema(description = "현재 상태 (UPCOMING, ONGOING, ENDED)")
    private String popStatus;

    @Schema(description = "인스타그램 URL")
    private String popInstaUrl;
}