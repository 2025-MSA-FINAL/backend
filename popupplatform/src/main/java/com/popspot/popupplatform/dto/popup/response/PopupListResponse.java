package com.popspot.popupplatform.dto.popup.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopupListResponse {

    @Schema(description = "팝업 카드 리스트")
    private List<com.popspot.popupplatform.dto.popup.response.PopupListItemResponse> content;

    @Schema(description = "다음 페이지 조회를 위한 커서 (다음 요청에 그대로 전달)", example = "2025-12-31T00:00:00_105")
    private String nextCursor;

    @Schema(description = "다음 데이터가 더 존재하는지 여부", example = "true")
    private boolean hasNext;
}
