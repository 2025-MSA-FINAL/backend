package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "소셜 회원가입 요청 DTO")
public class SocialSignupRequest {
    private String name;
    private String email;
    private String nickname;
    private String gender;
    private String phone;
    private Integer birthYear;

    // 일반로그인 정보 추가 자동화 가능
    private String loginId;
    private String password;

    /** 업로드 API에서 받은 값(선택): 업로드 안 했다면 null */
    private String profileImageUrl;
    private String profileImageKey;
}