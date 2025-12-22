package com.popspot.popupplatform.dto.chat.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduledMessageResponse {
    private Long csmId;
    private String roomType;
    private Long roomId;

    private String content;
    private String csmType;

    private LocalDateTime scheduledAt;
    private String csmStatus;

    private LocalDateTime createdAt;
}
