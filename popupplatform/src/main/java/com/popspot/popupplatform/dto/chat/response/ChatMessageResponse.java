package com.popspot.popupplatform.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private String senderStatus; // 'ACTIVE', 'DELETED'

    private String content;
    private String messageType;
    private String aiMode; // RAG | PURE_LLM | NEED_CONFIRM (AI 메시지일 때만)
    private Object needConfirm;

    private List<String> imageUrls;
    private LocalDateTime createdAt;

    private Boolean isRead; // 현재 유저 기준 읽음 여부
    private String clientMessageKey;
    private Integer totalUserCount;
}
