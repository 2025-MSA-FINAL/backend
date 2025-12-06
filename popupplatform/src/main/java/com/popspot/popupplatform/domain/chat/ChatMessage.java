package com.popspot.popupplatform.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private Long cmId; //메세지ID
    private String cmType; //채팅방유형 "PRIVATE" OR "GROUP"
    private Long cmRoomId; //채팅방ID
    private Long userId; //메세지보낸userID
    private String cmContent; //메세지내용
    private Boolean cmIsDeleted; // 삭제유무
    private String cmUrl; // 이미지 URL
    private LocalDateTime createdAt; // 메시지 생성 시간
    private LocalDateTime updatedAt; // 수정 시간
}
