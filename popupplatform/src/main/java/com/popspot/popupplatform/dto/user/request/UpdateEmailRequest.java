package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "이메일 변경 요청 DTO")
public class UpdateEmailRequest {

    @Schema(description = "새 이메일", example = "example@naver.com")
    private String email;
}
