package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.AdminChatRoomDTO;
import com.popspot.popupplatform.dto.admin.AdminChatRoomStatsDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.service.admin.AdminChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/chatrooms")
@RequiredArgsConstructor
@Tag(name = "Admin ChatRoom Management", description = "관리자 채팅방 관리 API")
public class AdminChatRoomController {

    private final AdminChatRoomService chatRoomService;

    @GetMapping("/stats")
    @Operation(summary = "채팅방 통계 조회")
    public ResponseEntity<AdminChatRoomStatsDTO> getChatRoomStats() {
        AdminChatRoomStatsDTO stats = chatRoomService.getChatRoomStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping
    @Operation(summary = "채팅방 목록 조회")
    public ResponseEntity<PageDTO<AdminChatRoomDTO>> getChatRoomList(
            @RequestParam(required = false, defaultValue = "false") Boolean isDeleted,
            @ModelAttribute PageRequestDTO pageRequest
    ) {
        PageDTO<AdminChatRoomDTO> chatRooms = chatRoomService.getChatRoomList(isDeleted, pageRequest);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "채팅방 상세 조회")
    public ResponseEntity<AdminChatRoomDTO> getChatRoomDetail(@PathVariable Long chatId) {
        AdminChatRoomDTO chatRoom = chatRoomService.getChatRoomDetail(chatId);
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chatRoom);
    }

    @DeleteMapping("/{chatId}")
    @Operation(summary = "채팅방 삭제")
    public ResponseEntity<String> deleteChatRoom(@PathVariable Long chatId) {
        boolean success = chatRoomService.deleteChatRoom(chatId);
        return success ? ResponseEntity.ok("deleted") : ResponseEntity.badRequest().body("fail");
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "채팅방 일괄 삭제")
    public ResponseEntity<String> bulkDeleteChatRooms(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        int deletedCount = 0;

        for (String id : idArray) {
            try {
                Long chatId = Long.parseLong(id.trim());
                if (chatRoomService.deleteChatRoom(chatId)) {
                    deletedCount++;
                }
            } catch (NumberFormatException e) {
                log.error("Invalid chat ID: {}", id);
            }
        }

        return ResponseEntity.ok(deletedCount + " chatrooms deleted");
    }

    @GetMapping("/search")
    @Operation(summary = "채팅방 검색")
    public ResponseEntity<PageDTO<AdminChatRoomDTO>> searchChatRooms(
            @RequestParam String keyword,
            @ModelAttribute PageRequestDTO pageRequest
    ) {
        PageDTO<AdminChatRoomDTO> chatRooms = chatRoomService.searchChatRooms(keyword, pageRequest);
        return ResponseEntity.ok(chatRooms);
    }
}
