package com.popspot.popupplatform.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate template;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            // chat-room-PRIVATE-7
            String[] parts = channel.split("-");
            String roomType = parts[2];
            String roomId = parts[3];

            Object payload = objectMapper.readTree(body);

            String destination =
                    "/sub/chat-room-" + roomType + "-" + roomId;

            template.convertAndSend(destination, payload);

            log.info("🚀 STOMP PUSH {}", destination);

        } catch (Exception e) {
            log.error("RedisSubscriber ERROR", e);
        }
    }
}


