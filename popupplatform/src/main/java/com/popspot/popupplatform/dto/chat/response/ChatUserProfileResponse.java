package com.popspot.popupplatform.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "사용자 미니프로필 DTO")
public class ChatUserProfileResponse {
    private Long userId; //로그인userId
    private String profileImage;
    private String nickname;
    private String introduction; //자기소개
    private String status; //deleted active
}
