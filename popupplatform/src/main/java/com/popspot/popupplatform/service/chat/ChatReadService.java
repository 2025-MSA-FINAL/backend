package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final RedisTemplate<String, Object> redisTemplate;

    private String userLastReadKey(String roomType, Long roomId, Long userId) {
        return "chat:read:" + roomType + ":" + roomId + ":" + userId;
    }

    // 마지막 읽은 메시지 ID 저장
    public void updateLastRead(String roomType, Long roomId, Long userId, Long messageId) {
        // ✅ Long → String으로 저장 (StringRedisSerializer 대응)
        redisTemplate.opsForValue().set(
                userLastReadKey(roomType, roomId, userId),
                String.valueOf(messageId)
        );

    }

    // 유저의 마지막 읽은 메시지 ID 조회
    public Long getLastRead(String roomType, Long roomId, Long userId) {
        Object value = redisTemplate.opsForValue().get(userLastReadKey(roomType, roomId, userId));
        return value == null ? 0L : Long.parseLong(value.toString());
    }
}
