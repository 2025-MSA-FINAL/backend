package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "휴대폰 번호 변경 요청 DTO")
public class UpdatePhoneRequest {

    @Schema(description = "새 휴대폰 번호", example = "01012345678")
    private String phone;
}
