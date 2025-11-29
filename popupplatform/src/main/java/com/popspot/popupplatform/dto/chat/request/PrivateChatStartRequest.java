package com.popspot.popupplatform.dto.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "1:1 채팅 시작 요청 DTO")
public class PrivateChatStartRequest {
    private Long targetUserId;
}
