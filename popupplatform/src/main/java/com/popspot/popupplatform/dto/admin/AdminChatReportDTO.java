package com.popspot.popupplatform.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

// 채팅방 신고 리스트 DTO
@Data
public class AdminChatReportDTO {
    private Long reportId;
    private Long chatId;
    private Long reporterId;
    private String reporterName;
    private Long targetUserId;
    private String targetUserName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
