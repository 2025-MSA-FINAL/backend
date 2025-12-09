package com.popspot.popupplatform.dto.report;

import lombok.Data;

@Data
public class ReportListDTO {
    private Long repId;
    private String repType;
    private String categoryName;
    private String reporterName;
    private String userNickname;
    private String createdAt;
    private String repStatus;
    private String targetName; //신고 대상 이름 (user: 유저 닉네임, chat: 채팅방 이름)
    private String targetNickname;; //신고 대상 유저 닉네임 (user 타입일 때만)
}