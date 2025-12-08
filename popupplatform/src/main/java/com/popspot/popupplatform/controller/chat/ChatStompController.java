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
    public void sendMessage(ChatMessageRequest request) throws Exception {

        var savedMessage = chatMessageService.saveMessage(request);

        String channel = "chat-room-" + request.getRoomType() + "-" + request.getRoomId();
        redisPublisher.publish(channel, objectMapper.writeValueAsString(savedMessage));

        log.info("📨 메시지 전송 완료 → Redis Publish");
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

        String channel = "chat-room-" + req.getRoomType() + "-" + req.getRoomId();
        redisPublisher.publish(channel, objectMapper.writeValueAsString(payload));

        log.info("👁 읽음 처리 → user={} room={} msg={}",
                userId, req.getRoomId(), req.getLastReadMessageId());
    }

    // 읽음 이벤트 response DTO
    record ReadReceiptPayload(Long roomId, String roomType, Long messageId, Long readerUserId) {}
}
