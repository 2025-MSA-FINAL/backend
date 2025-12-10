package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.*;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

import java.util.List;

public interface AdminChatRoomService {

    // ===== 통계 =====
    AdminChatRoomStatsDTO getChatRoomStats();

    // ===== 채팅방 목록 (정렬 포함) =====
    PageDTO<AdminChatRoomDTO> getChatRoomList(
            Boolean isDeleted,
            String sort,
            PageRequestDTO pageRequest
    );

    // ===== 채팅방 검색 (정렬 포함) =====
    PageDTO<AdminChatRoomDTO> searchChatRooms(
            String keyword,
            Boolean isDeleted,
            String searchType,
            String sort,
            PageRequestDTO pageRequest
    );

    // ===== 채팅방 상세 =====
    AdminChatRoomDTO getChatRoomDetail(Long chatId);

    // ===== 채팅방 삭제 / 복구 =====
    boolean deleteChatRoom(Long chatId);
    boolean restoreChatRoom(Long chatId);

    // ===== 참여자 관리 =====
    List<AdminChatParticipantDTO> getChatRoomParticipants(Long chatId);

    // ===== 채팅방 신고 =====
    List<AdminChatReportDTO> getChatRoomReports(Long chatId);
    boolean updateChatReportStatus(Long reportId, String status);
}
