package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "닉네임 변경 요청 DTO")
public class UpdateNicknameRequest {

    @Schema(description = "새 닉네임", example = "팝업덕후")
    private String nickname;
}
