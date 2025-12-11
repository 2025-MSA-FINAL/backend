package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자 - 채팅방 목록 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChatRoomDTO {
    private Long chatId;
    private String chatName;           // 그룹채팅방명
    private Long popId;                // 팝업스토어 ID
    private String popupName;          // 팝업스토어명
    private Long hostUserId;           // 방장 유저 ID
    private String hostUserName;       // 방장 이름
    private String hostNickname;       // 방장 닉네임
    private Integer participantCount;  // 현재 참여자 수
    private Integer maxParticipants;   // 최대 참여자 수
    private Integer messageCount;      // 메시지 수
    private Boolean hasReports;        // 신고 발생 여부
    private Boolean chatIsDeleted;     // 삭제 여부
    private LocalDateTime createdAt;   // 생성일
}
