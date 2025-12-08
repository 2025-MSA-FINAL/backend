package com.popspot.popupplatform.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;

    public void publish(String channel, String message) {
        log.info("📤 Redis Publish ({}) : {}", channel, message);
        redisTemplate.convertAndSend(channel, message);
    }
}