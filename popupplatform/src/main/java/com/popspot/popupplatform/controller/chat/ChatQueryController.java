package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.response.ChatRoomSummaryResponse;
import com.popspot.popupplatform.dto.chat.response.ChatUserProfileResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.ChatRoomQueryService;
import com.popspot.popupplatform.service.chat.ChatUserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "채팅방 관련 조회 API")
public class ChatQueryController {
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatUserQueryService chatUserQueryService;

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

    @GetMapping("/users/{userId}")
    @Operation(
            summary = "채팅용 사용자 프로필 조회",
            description = """
                채팅 화면에서 사용자의 미니 프로필을 보기 위한 API입니다.
                - userId로 사용자 사진, 닉네임, 소개, 상태를 조회합니다.
                - 로그인 여부와 관계없이 공개 가능한 정보만 반환됩니다.
                """
    )
    public ChatUserProfileResponse getChatUserProfile(@PathVariable Long userId) {
        return chatUserQueryService.getChatUserProfile(userId);
    }
}
