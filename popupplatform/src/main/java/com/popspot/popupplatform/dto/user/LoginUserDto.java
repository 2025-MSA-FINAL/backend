// src/main/java/com/popspot/popupplatform/dto/auth/LoginUserDto.java
package com.popspot.popupplatform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * USER_GENERAL + USER 조인 결과
 * 일반 로그인 시 사용할 최소 정보
 * 토큰에 넣어줄 용도
 */
@Data
@Schema(description = "로그인한 사용자의 최소 정보 DTO")
public class LoginUserDto {
    private Long userId;
    private String loginId;
    private String password; // 해시된 비밀번호 (BCrypt 등)
    private String role;     // USER.user_role
    private String status;   // USER.user_status
}
