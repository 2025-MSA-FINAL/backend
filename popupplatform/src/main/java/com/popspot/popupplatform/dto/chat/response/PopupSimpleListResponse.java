package com.popspot.popupplatform.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "팝업 간단 목록 + 총 개수 응답 DTO")
public class PopupSimpleListResponse {
    private int count; //총 팝업 개수
    private List<PopupSimpleResponse> popups; //팝업 목록
}
