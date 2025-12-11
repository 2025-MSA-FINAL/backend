package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "비밀번호 변경 요청 DTO")
public class ChangePasswordRequest {

    @Schema(description = "현재 비밀번호")
    private String currentPassword;

    @Schema(description = "새 비밀번호")
    private String newPassword;
}
