// src/main/java/com/popspot/popupplatform/dto/auth/LoginRequest.java
package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 일반 로그인 요청 DTO
 * - 일반 회원가입 기능은 없고, 이미 DB에 존재하는 USER_GENERAL 기준으로만 로그인한다.
 */
@Data
@Schema(description = "일반 로그인 요청 DTO")
public class LoginRequest {
    private String loginId;
    private String password;
}
