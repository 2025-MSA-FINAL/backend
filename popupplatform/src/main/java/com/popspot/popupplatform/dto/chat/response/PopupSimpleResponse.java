package com.popspot.popupplatform.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "팝업 간단 목록 응답 DTO")
public class PopupSimpleResponse {
    private Long popId; //팝업 ID
    private String popName; //팝업 이름
}
