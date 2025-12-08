package com.popspot.popupplatform.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String createdAt; // "오전 10:40" 같은 형태
    private String dateLabel; // 날짜 구분선: "2025년 11월 21일 금요일"

    private Integer readCount; // 읽은 사람 수
    private Boolean isRead; // 현재 유저 기준 읽음 여부
    private Boolean unreadSeparator; // 구분선 추가 여부
}
