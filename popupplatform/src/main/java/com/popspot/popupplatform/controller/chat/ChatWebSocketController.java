package com.popspot.popupplatform.controller.chat;


import com.popspot.popupplatform.domain.chat.ChatMessage;
import com.popspot.popupplatform.dto.chat.request.ChatMessageSendRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.infra.redis.ChatMessagePublisher;
import com.popspot.popupplatform.service.chat.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final ChatMessageService chatMessageService;
    private final ChatMessagePublisher chatMessagePublisher;

    @MessageMapping("/chat/message")
    @Operation(summary = "채팅 메시지 STOMP 수신 (WebSocket용)")
    public void handleChatMessage(@Payload ChatMessageSendRequest request) {
        log.debug("STOMP MESSAGE 수신: {}",request);

        //DB저장
        ChatMessage saved = chatMessageService.saveMessage(request);
        //DB에 저장된 엔티티->응답DTO로변환
        ChatMessageResponse response = chatMessageService.toResponse(saved);
        //redis publish
        chatMessagePublisher.publish(response);
    }
}
