package com.popspot.popupplatform.dto.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class ChatMessageSendRequest {
    private String roomType; //보낸채팅방유형
    private Long roomId; //보낸채팅방ID
    private Long senderId; //보낸userId
    private String content; //보낸메세지내용
    private String imgUrl; //보낸이미지URL
}
