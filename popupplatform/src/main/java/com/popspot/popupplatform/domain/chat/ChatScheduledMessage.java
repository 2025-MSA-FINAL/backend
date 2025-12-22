package com.popspot.popupplatform.domain.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatScheduledMessage {
    private Long csmId;
    private String roomType; // GROUP / PRIVATE
    private Long roomId; // gcrId / pcrId
    private Long senderId; //userId
    private String content;
    private String csmType; // TEXT / IMAGE
    private LocalDateTime scheduledAt; // 전송예정 시각
    private String csmStatus; // PENDING / SENT / CANCELED
    private LocalDateTime createdAt; // 예약한 시간
    private LocalDateTime updatedAt;
}