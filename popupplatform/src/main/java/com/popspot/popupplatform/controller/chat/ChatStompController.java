package com.popspot.popupplatform.controller.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.request.ChatReadRequest;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.global.redis.RedisPublisher;
import com.popspot.popupplatform.service.chat.ChatMessageService;
import com.popspot.popupplatform.service.chat.ChatReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest request) {
        chatMessageService.saveMessage(request);
    }

    @MessageMapping("/chat.read")
    public void readMessage(ChatReadRequest req,
                            @AuthenticationPrincipal CustomUserDetails user) throws Exception {

        Long userId = user.getUserId();

        chatReadService.updateLastRead(
                req.getRoomType(),
                req.getRoomId(),
                userId,
                req.getLastReadMessageId()
        );

        // 읽음 이벤트 브로드캐스트
        ReadReceiptPayload payload = new ReadReceiptPayload(
                req.getRoomId(),
                req.getRoomType(),
                req.getLastReadMessageId(),
                userId
        );

        redisPublisher.publish(
                "chat-room-" + req.getRoomType() + "-" + req.getRoomId(),
                objectMapper.writeValueAsString(payload)
        );
    }

    // 읽음 이벤트 response DTO
    record ReadReceiptPayload(Long roomId, String roomType, Long messageId, Long readerUserId) {}

    //타이핑API
    @MessageMapping("/chat/typing")
    public void typing(ChatTypingPayload payload) throws Exception {
        redisPublisher.publish(
                "chat-room-" + payload.roomType() + "-" + payload.roomId(),
                objectMapper.writeValueAsString(payload)
        );
    }
    // 타이핑 이벤트 DTO
    public record ChatTypingPayload(
            String type,       // TYPING_START / TYPING_STOP
            String roomType,
            Long roomId,
            Long senderId
    ) {}
}
