package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 - 채팅방 통계 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChatRoomStatsDTO {
    private Long totalChatRooms;      // 전체 채팅방 수
    private Long activeChatRooms;     // 활성 채팅방 수 (삭제 안 된)
    private Long inactiveChatRooms;   // 비활성 채팅방 수 (참여자 0명 or 활동 없음)
    private Long reportedChatRooms;   // 신고된 채팅방 수
}
