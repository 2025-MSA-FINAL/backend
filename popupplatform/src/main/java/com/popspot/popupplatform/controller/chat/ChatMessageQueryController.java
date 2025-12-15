package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.response.ChatMessageListResponse;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatParticipantResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.mapper.chat.ChatParticipantMapper;
import com.popspot.popupplatform.service.chat.ChatMessageService;
import com.popspot.popupplatform.service.chat.ChatReadService;
import com.popspot.popupplatform.service.chat.PrivateChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages")
public class ChatMessageQueryController {

    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final ChatParticipantMapper participantMapper;
    private final PrivateChatRoomService privateChatRoomService;
    /**
     * 채팅 메시지 불러오기 API
     * 예)
     * /api/chat/messages?roomType=GROUP&roomId=1&limit=20
     * /api/chat/messages?roomType=GROUP&roomId=1&lastMessageId=100&limit=20
     */
    @GetMapping
    public ResponseEntity<ChatMessageListResponse> getMessages(
            @RequestParam String roomType,
            @RequestParam Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();

        List<ChatMessageResponse> messages =
                chatMessageService.getMessages(roomType, roomId, lastMessageId, limit, userId);

        Long lastReadId = chatReadService.getLastRead(roomType, roomId, userId);

        Long otherLastReadId = 0L;
        if ("PRIVATE".equals(roomType)) {
            Long otherUserId = privateChatRoomService.getOtherUserId(roomId, userId);
            otherLastReadId = chatReadService.getLastRead("PRIVATE", roomId, otherUserId);
        }
        List<GroupChatParticipantResponse> participants = null;
        if ("GROUP".equals(roomType)) {
            participants = participantMapper.findParticipants(roomId);
        }

        return ResponseEntity.ok(
                new ChatMessageListResponse(
                        messages,
                        lastReadId,
                        otherLastReadId,
                        participants
                )
        );
    }
}
