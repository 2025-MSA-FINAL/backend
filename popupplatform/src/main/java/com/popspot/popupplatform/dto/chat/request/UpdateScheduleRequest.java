package com.popspot.popupplatform.dto.chat.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateScheduleRequest {
    private String content;          // 수정할 메시지 내용
    private LocalDateTime scheduledAt; // 수정할 예약 시간
}
