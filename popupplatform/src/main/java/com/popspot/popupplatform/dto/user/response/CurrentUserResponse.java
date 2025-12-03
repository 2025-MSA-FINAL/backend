package com.popspot.popupplatform.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "현재 로그인한 사용자 정보(네비게이션 바 등에서 사용)")
public class CurrentUserResponse {

    private Long userId;

    private String name;
    private String nickname;
    private String introduction;

    private String profileImage; // USER.user_photo
    private String email;
    private String phone;
    private String gender;

    private String role;   // USER.user_role
    private String status; // USER.user_status
}
