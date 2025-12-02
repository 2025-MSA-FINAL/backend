package com.popspot.popupplatform.dto.user.response;

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
@Schema(description = "매니저용 예약자 목록 응답")
public class ManagerReservationResponse {

    @Schema(description = "예약 ID (취소 시 사용)")
    private Long reservationId;

    @Schema(description = "예약자 유저 ID")
    private Long userId;

    @Schema(description = "예약자 이름 (실명)")
    private String userName;

    @Schema(description = "예약자 닉네임")
    private String userNickname;

    @Schema(description = "예약자 프로필 사진")
    private String userProfileImage;

    @Schema(description = "예약자 전화번호")
    private String userPhone;

    @Schema(description = "예약된 방문 날짜 및 시간")
    private LocalDateTime reservedDateTime;

    @Schema(description = "예약 인원 수")
    private Integer userCount;

    @Schema(description = "예약 상태 (true: 확정, false: 취소)")
    private Boolean status;
}