package com.popspot.popupplatform.dto.popup.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopupWishlistToggleResponse {

    @Schema(description = "찜 토글 결과 (true: 찜 설정됨, false: 찜 해제됨)", example = "true")
    private Boolean isLiked;
}