package com.popspot.popupplatform.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "STOMP 메시지 전송 DTO")
public class ChatMessageDto {
    private String roomType; //채팅방타입 "PRIVATE" OR "GROUP"
    private Long roomId; //채팅방ID pcr_id OR gcr_id
    private Long senderId; //메세지보낸사람 userID
    private String content; //메세지내용
    private LocalDateTime createdAt;
}
