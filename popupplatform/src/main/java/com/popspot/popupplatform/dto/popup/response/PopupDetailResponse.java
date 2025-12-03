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
@Schema(description = "팝업 스토어 상세 조회 응답 DTO")
public class PopupDetailResponse {

    // --- 기본 정보 ---
    @Schema(description = "팝업 ID", example = "101")
    private Long popId;

    @Schema(description = "등록자(매니저) ID", example = "55")
    private Long popOwnerId;

    @Schema(description = "팝업 제목", example = "신라면 팝업스토어: 더 레드")
    private String popName;

    @Schema(description = "팝업 상세 설명 (줄바꿈 포함)", example = "매운맛의 진수를 보여주는 신라면 팝업스토어입니다...")
    private String popDescription;

    @Schema(description = "대표 썸네일 이미지 URL (상단 배너용)", example = "https://bucket.s3.../thumb.jpg")
    private String popThumbnail;

    // --- 장소 및 기간 ---
    @Schema(description = "팝업 장소 (도로명 주소)", example = "서울 성동구 연무장길 1")
    private String popLocation;

    @Schema(description = "운영 시작일", example = "2024-11-01T10:00:00")
    private LocalDateTime popStartDate;

    @Schema(description = "운영 종료일", example = "2024-11-15T22:00:00")
    private LocalDateTime popEndDate;

    @Schema(description = "공식 인스타그램 URL", example = "https://instagram.com/shinramen")
    private String popInstaUrl;

    // --- 운영 정보 ---
    @Schema(description = "예약 필수 여부 (true: 예약 버튼 활성화, false: 현장 대기)", example = "true")
    private Boolean popIsReservation;

    @Schema(description = "가격 타입 (FREE: 무료, PAID: 유료)", example = "FREE")
    private PopupPriceType popPriceType;

    @Schema(description = "입장료 (무료면 0원)", example = "0")
    private Integer popPrice;

    @Schema(description = "현재 상태 (UPCOMING: 오픈 예정, ONGOING: 진행 중, ENDED: 종료)", example = "ONGOING")
    private PopupStatus popStatus;

    // --- 통계 및 AI ---
    @Schema(description = "누적 조회수", example = "1540")
    private Long popViewCount;

    @Schema(description = "AI 요약 텍스트 (상단 회색 박스)", example = "성수동에서 가장 핫한 매운맛 체험, 한정판 굿즈 제공")
    private String popAiSummary;

    // --- 관계형 데이터 (1:N) ---
    @Schema(description = "상세 이미지 URL 리스트 (하단 슬라이드용)")
    private List<String> images;

    @Schema(description = "해시태그 리스트 (상단 태그 칩)", example = "[\"성수동\", \"매운맛\", \"데이트\"]")
    private List<String> hashtags;

    // --- 유저 상호작용 ---
    @Schema(description = "로그인 유저 찜 여부 (비로그인: null)", example = "true")
    private Boolean isLiked;

    // --- 예약 정보 ---
    @Schema(description = "예약 시작 가능 일시", example = "2025-11-17T14:00:00")
    private LocalDateTime reservationStartTime;

    @Schema(description = "예약 상태 (UPCOMING/OPEN/CLOSED 등)", example = "UPCOMING")
    private String reservationStatus;

}