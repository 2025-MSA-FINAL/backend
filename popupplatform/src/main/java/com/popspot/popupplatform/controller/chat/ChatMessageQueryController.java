package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.ChatMessageService;
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

    /**
     * 채팅 메시지 불러오기 API
     * 예)
     * /api/chat/messages?roomType=GROUP&roomId=1&limit=20
     * /api/chat/messages?roomType=GROUP&roomId=1&lastMessageId=100&limit=20
     */
    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @RequestParam String roomType,
            @RequestParam Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();

        return ResponseEntity.ok(
                chatMessageService.getMessages(roomType, roomId, lastMessageId, limit, userId)
        );
    }
}
