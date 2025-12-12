package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "자기소개 변경 요청 DTO")
public class UpdateIntroductionRequest {
    @Schema(description = "새 자기소개", example = "팝업덕후")
    private String introduction;
}
