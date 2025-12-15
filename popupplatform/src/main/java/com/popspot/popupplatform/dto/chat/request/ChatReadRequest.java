package com.popspot.popupplatform.dto.chat.request;

import lombok.Data;

@Data
public class ChatReadRequest {
    private String roomType;          // PRIVATE / GROUP
    private Long roomId;
    private Long lastReadMessageId;   // 사용자가 마지막으로 읽은 메시지 ID
    private Long senderId;
}
