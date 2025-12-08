package com.popspot.popupplatform.dto.chat.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long cmId;
    private String roomType;   // "GROUP" 또는 "PRIVATE"
    private Long roomId;       // gcr_id 또는 pcr_id
    private Long senderId;     // user_id
    private String content;    // 메시지 내용
    private String messageType; // TEXT / IMAGE / NOTICE 등
}
