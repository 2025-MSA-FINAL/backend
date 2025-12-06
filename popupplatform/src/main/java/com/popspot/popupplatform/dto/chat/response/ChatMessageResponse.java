package com.popspot.popupplatform.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponse {
    private Long cmId; //메세지ID
    private String roomType; //수신한채팅방유형 "PRIVATE" OR "GROUP"
    private Long roomId; //수신한채팅방ID
    private Long senderId; //메세지수신자userID
    private String content; //받을메세지내용
    private String imgUrl; // 받을이미지 URL
    private LocalDateTime createdAt; // 메시지 생성 시간
}
