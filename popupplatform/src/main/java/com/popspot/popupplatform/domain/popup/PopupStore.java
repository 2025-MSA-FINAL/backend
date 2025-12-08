package com.popspot.popupplatform.domain.popup;

import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DB 테이블 'POPUPSTORE'와 1:1 매핑되는 객체 (Entity)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PopupStore {
    private Long popId;                     // PK
    private Long popOwnerId;                // 매니저 ID
    private String popName;                 // 제목
    private String popDescription;          // 내용
    private String popThumbnail;            // 썸네일 이미지 URL
    private String popLocation;             // 장소
    private LocalDateTime popStartDate;     // 시작일
    private LocalDateTime popEndDate;       // 종료일
    private Boolean popIsReservation;       // 예약 여부
    private PopupPriceType popPriceType;    // 가격 타입
    private Integer popPrice;               // 가격
    private PopupStatus popStatus;          // 상태
    private String popInstaUrl;             // 인스타 URL
    private Boolean popModerationStatus;    // 관리자 승인 여부
    private Boolean popIsDeleted;           // 삭제 여부 (Soft Delete)
    private Long popViewCount;              // 조회수
    private String popAiSummary;            // AI 요약 내용

    private Long popPopularityScore;        // 인기 점수: 최근 7일 조회수 + 찜 * 3
}