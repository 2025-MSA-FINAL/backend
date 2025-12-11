package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class UserListDTO {
    private Long userId;
    private String userName;
    private String userNickname;
    private String userEmail;
    private String userPhonenumber;
    private String userGender;
    private Integer userBirthyear;
    private String userStatus;         // active, suspended, deleted
    private String userRole;           // user, manager, admin
    private String userPhoto;
    private String createdAt;

    // 소셜 로그인 정보
    private String oauthProvider;      // naver, kakao, google
    private String oauthId;

    // 일반 로그인 정보
    private String loginId;

    // 통계 정보
    private Long reservationCount;     // 예약 수
    private Long wishlistCount;        // 찜한 팝업 수
}
