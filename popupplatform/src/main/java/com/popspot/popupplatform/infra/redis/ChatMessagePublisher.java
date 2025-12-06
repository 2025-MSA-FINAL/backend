package com.popspot.popupplatform.infra.redis;

import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;


//채팅 메시지를 Redis Pub/Sub 채널로 publish 하는 컴포넌트
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePublisher {
    // Redis 작업을 수행하기 위한 템플릿 (메시지 직렬화 JSON 및 채널 전송 기능)
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatTopic; //메시지를 전송할 Redis 채널의 토픽 정보

    //Redis Pub/Sub 채널에 메세지 전송
    //ChatMessageResponse 클라이언트에게 전송될 응답 DTO
    public void publish(ChatMessageResponse message) {
        log.debug("Publishing chat message to Redis: {}", message);
        //지정된 Topic과 메시지 객체를 Redis로 전송
        redisTemplate.convertAndSend(chatTopic.getTopic(), message);
    }
}
