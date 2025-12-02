package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.response.ChatRoomSummaryResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.ChatRoomQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "ChatRoom", description = "채팅방 목록 조회 API")
public class ChatRoomQueryController {
    private final ChatRoomQueryService chatRoomQueryService;

    @GetMapping("/my-rooms")
    @Operation(
            summary = "내가 참여한 전체 채팅방 목록",
            description = """
                내가 참여한 1:1 + 그룹 채팅방 목록을 모두 가져옵니다.
                - 숨김 처리된 방은 포함되지 않습니다.
                - 1:1 채팅에서 '나만 삭제'된 방도 포함되지 않습니다.
                - 정렬 기준: created_at DESC (추후 최근 메시지 기준으로 변경 예정)
                """
    )
    public ResponseEntity<List<ChatRoomSummaryResponse>> getMyRooms(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId(); //로그인한 userId를 가져오기
        //서비스에서 내채팅방목록 불러오기
        List<ChatRoomSummaryResponse> rooms = chatRoomQueryService.getMyChatRooms(userId);
        return ResponseEntity.ok(rooms);
    }
}
