package com.popspot.popupplatform.dto.popup.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor
@Schema(description = "팝업 스토어 등록 요청 DTO")
public class PopupCreateRequest {

    //필수 기본 정보
    @Schema(description = "팝업 제목", example = "성수동 조롱이 팝업")
    @NotBlank(message = "팝업 제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자를 넘을 수 없습니다.")
    private String popName;

    @Schema(description = "팝업 상세 설명", example = "이 팝업은...")
    @NotBlank(message = "상세 설명은 필수입니다.")
    @Size(max = 2000, message = "설명은 2000자를 넘을 수 없습니다.")
    private String popDescription;

    @Schema(description = "팝업 장소 (도로명 주소)", example = "서울 성동구...")
    @NotBlank(message = "장소는 필수입니다.")
    private String popLocation;

    //날짜 및 시간 정보
    @Schema(description = "시작 일시", example = "2025-01-01T10:00:00")
    @NotNull(message = "시작 일시는 필수입니다.")
    @FutureOrPresent(message = "시작 일시는 현재 또는 미래여야 합니다.")
    private LocalDateTime popStartDate;

    @Schema(description = "종료 일시", example = "2025-01-02T20:00:00")
    @NotNull(message = "종료 일시는 필수입니다.")
    @FutureOrPresent(message = "종료 일시는 현재 또는 미래여야 합니다.")
    private LocalDateTime popEndDate;

    //운영 정보
    @Schema(description = "입장료 (무료면 0)", example = "0")
    @NotNull(message = "입장료는 필수입니다. (없으면 0)")
    @Min(value = 0, message = "입장료는 0원 이상이어야 합니다.")
    private Integer popPrice;

    @Schema(description = "예약 필수 여부", example = "true")
    @NotNull
    private Boolean popIsReservation;

    @Schema(description = "인스타그램(홈페이지) URL", example = "https://instagram.com/...")
    private String popInstaUrl;

    //이미지 정보 (URL)
    @Schema(description = "팝업 썸네일 URL (파일 업로드 API(/api/files/popup)에서 받은 url)")
    @NotBlank(message = "썸네일 이미지는 필수입니다.")
    private String popThumbnail;

    @Schema(description = "팝업 상세 이미지 URL 목록 (/api/files/popup 응답의 url 목록)")
    private List<String> popImages;

    //해시태그
    @Schema(description = "해시태그 목록", example = "[\"#무료\", \"#데이트\", \"#20대\"]")
    @Size(max = 10, message = "해시태그는 최대 10개까지 입력 가능합니다.")
    private List<
            @Size(max = 30, message = "해시태그는 30자 이하여야 합니다.")
            @Pattern(regexp = "^#?\\S+$", message = "해시태그에는 공백이 포함될 수 없습니다.")
                    String
            > hashtags;

    //커스텀 검증 로직
    @AssertTrue(message = "종료 일시는 시작 일시보다 이후여야 합니다.")
    private boolean isValidDateRange() {
        if (popStartDate == null || popEndDate == null) {
            return true;
        }
        return popEndDate.isAfter(popStartDate);
    }
}