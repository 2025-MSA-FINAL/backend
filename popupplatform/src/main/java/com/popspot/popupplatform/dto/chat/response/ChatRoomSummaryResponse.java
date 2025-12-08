package com.popspot.popupplatform.dto.chat.response;

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
@Schema(description = "채팅방 요약 정보 DTO")
public class ChatRoomSummaryResponse {
    private String roomType; // "PRIVATE" 또는 "GROUP"
    private Long roomId; // pcr_id 또는 gcr_id
    private String roomName; // 상대닉네임 또는 gcr_title
    private Long otherUserId; // 1대1채팅방에서의 상대UserId (GROUP에서는 null)
    private LocalDateTime createdAt; // 정렬 기준: created_at
    private Integer unreadCount;   //안 읽은 메시지 개수
}
