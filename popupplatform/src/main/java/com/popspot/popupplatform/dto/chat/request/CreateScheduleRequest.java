package com.popspot.popupplatform.dto.chat.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateScheduleRequest {
    private String roomType;
    private Long roomId;
    private String content;
    private LocalDateTime scheduledAt;
}