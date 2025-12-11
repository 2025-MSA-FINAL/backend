// src/main/java/com/popspot/popupplatform/dto/user/UserDto.java
package com.popspot.popupplatform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * USER + USER_SOCIAL 조인 결과를 담는 DTO
 * - USER: 기본 프로필/상태/권한
 * - USER_SOCIAL: 소셜 프로바이더 정보
 */
@Data
@Schema(description = "회원 기본 정보 및 소셜 로그인 정보를 담는 DTO")
public class UserDto {

    // USER
    private Long userId;        // USER.user_id
    private String name;        // USER.user_name
    private String nickname;    // USER.user_nickname
    private String phone;       // USER.user_phonenumber
    private String gender;      // USER.user_gender
    private Integer birthYear;  // USER.user_birthyear
    private String profileImage; // USER.user_photo (URL)
    private String email;       // USER.user_email
    private String status;      // USER.user_status (예: ACTIVE / BLOCK / DELETED)
    private String role;        // USER.user_role (예: USER / MANAGER / ADMIN)
    private String introduction; // 자기소개

    // USER_SOCIAL
    private String provider;    // USER_SOCIAL.oauth_provider (예: NAVER)
    private String providerId;  // USER_SOCIAL.oauth_id

    // USER_GENERAL
    private String loginId;
    private String loginPwd;
}
