package com.popspot.popupplatform.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long cmId;
    private Long roomId;
    private String roomType;

    private Long senderId;
    private String senderNickname;
    private String senderProfileUrl;

    private String content;
    private String messageType;

    private LocalDateTime createdAt;

    private Integer readCount; // 읽은 사람 수
    private Boolean isRead; // 현재 유저 기준 읽음 여부
    private Boolean unreadSeparator; // 구분선 추가 여부
}
