package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.*;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.service.admin.AdminChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chatrooms")
@RequiredArgsConstructor
public class AdminChatRoomController {

    private final AdminChatRoomService chatRoomService;

    /**
     * 채팅방 통계 조회
     * GET /api/admin/chatrooms/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminChatRoomStatsDTO> getChatRoomStats() {
        return ResponseEntity.ok(chatRoomService.getChatRoomStats());
    }

    /**
     * 채팅방 목록 조회 (통합 검색/필터/정렬)
     * GET /api/admin/chatrooms?page=0&size=10&keyword=&isDeleted=false&sort=createdAt
     *
     * @param pageRequest 페이징 정보
     * @param keyword 검색어 (optional)
     * @param isDeleted 삭제 여부 (optional)
     * @param sort 정렬 기준 (createdAt, reportCount, participantCount, messageCount, name)
     */
    @GetMapping
    public ResponseEntity<PageDTO<AdminChatRoomDTO>> getChatRoomList(
            PageRequestDTO pageRequest,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "all") String searchType) { // searchType 추가!

        PageDTO<AdminChatRoomDTO> result;

        if (keyword != null && !keyword.trim().isEmpty()) {
            result = chatRoomService.searchChatRooms(
                    keyword,
                    isDeleted,
                    searchType,
                    sort,
                    pageRequest); // searchType 전달
        } else {
            // ... (기존 목록 조회 로직)
            result = chatRoomService.getChatRoomList(isDeleted, sort, pageRequest);
        }

        return ResponseEntity.ok(result);
    }
    /**
     * 채팅방 상세 조회
     * GET /api/admin/chatrooms/{chatId}
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<AdminChatRoomDTO> getChatRoomDetail(@PathVariable Long chatId) {
        AdminChatRoomDTO chatRoom = chatRoomService.getChatRoomDetail(chatId);
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 채팅방 삭제
     * DELETE /api/admin/chatrooms/{chatId}
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteChatRoom(@PathVariable Long chatId) {
        boolean success = chatRoomService.deleteChatRoom(chatId);
        return success ?
                ResponseEntity.ok("deleted") :
                ResponseEntity.badRequest().body("fail");
    }

    /**
     * 채팅방 복구
     * PUT /api/admin/chatrooms/{chatId}/restore
     */
    @PutMapping("/{chatId}/restore")
    public ResponseEntity<String> restoreChatRoom(@PathVariable Long chatId) {
        boolean success = chatRoomService.restoreChatRoom(chatId);
        return success ?
                ResponseEntity.ok("restored") :
                ResponseEntity.badRequest().body("fail");
    }

    /**
     * 채팅방 참여자 목록
     * GET /api/admin/chatrooms/{chatId}/participants
     */
    @GetMapping("/{chatId}/participants")
    public ResponseEntity<List<AdminChatParticipantDTO>> getChatRoomParticipants(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomParticipants(chatId));
    }

    /**
     * 채팅방 신고 목록
     * GET /api/admin/chatrooms/{chatId}/reports
     */
    @GetMapping("/{chatId}/reports")
    public ResponseEntity<List<AdminChatReportDTO>> getChatRoomReports(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomReports(chatId));
    }

    /**
     * 채팅방 신고 상태 변경
     * PUT /api/admin/chatrooms/reports/{reportId}/status?status=approved
     */
    @PutMapping("/reports/{reportId}/status")
    public ResponseEntity<String> updateChatReportStatus(
            @PathVariable Long reportId,
            @RequestParam String status) {
        boolean success = chatRoomService.updateChatReportStatus(reportId, status);
        return success ?
                ResponseEntity.ok("updated") :
                ResponseEntity.badRequest().body("fail");
    }
}