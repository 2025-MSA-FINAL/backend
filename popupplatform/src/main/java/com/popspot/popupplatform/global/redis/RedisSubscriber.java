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
            String msg = new String(message.getBody());

            log.info("🔥 RedisSubscriber Received [{}] : {}", channel, msg);

            template.convertAndSend("/sub/" + channel, msg);

        } catch (Exception e) {
            log.error("RedisSubscriber ERROR: {}", e.getMessage());
        }
    }
}

