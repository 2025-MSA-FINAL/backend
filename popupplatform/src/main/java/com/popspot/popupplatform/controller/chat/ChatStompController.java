package com.popspot.popupplatform.controller.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.request.ChatReadRequest;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.global.redis.RedisPublisher;
import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import com.popspot.popupplatform.mapper.chat.ChatParticipantMapper;
import com.popspot.popupplatform.service.chat.ChatMessageService;
import com.popspot.popupplatform.service.chat.ChatReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatParticipantMapper participantMapper;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest request) {
        chatMessageService.saveMessage(request);
    }

    @MessageMapping("/chat/read")
    public void readMessage(ChatReadRequest req) throws Exception {

        Long userId = req.getSenderId(); // 🔥 다시 사용
        if (userId == null) return;
        if (userId.equals(20251212L)) return;

        Long messageSenderId =
                chatMessageMapper.getSenderIdByMessageId(req.getLastReadMessageId());

        // 자기 메시지 읽음 방지
        if (userId.equals(messageSenderId)) return;

        if ("GROUP".equals(req.getRoomType())) {
            participantMapper.updateLastRead(
                    req.getRoomId(),
                    userId,
                    req.getLastReadMessageId()
            );
        }

        chatReadService.updateLastRead(
                req.getRoomType(),
                req.getRoomId(),
                userId,
                req.getLastReadMessageId()
        );

        redisPublisher.publish(
                "chat-room-" + req.getRoomType() + "-" + req.getRoomId(),
                objectMapper.writeValueAsString(
                        Map.of(
                                "type", "READ",
                                "readerUserId", userId,
                                "lastReadMessageId", req.getLastReadMessageId()
                        )
                )
        );
    }

    // 읽음 이벤트 response DTO
    record ReadReceiptPayload(
            String type,
            String roomType,
            Long roomId,
            Long lastReadMessageId,
            Long readerUserId
    ) {}
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
            Long senderId,
            String senderNickname
    ) {}
}
