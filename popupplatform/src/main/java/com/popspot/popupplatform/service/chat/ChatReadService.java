package com.popspot.popupplatform.service.chat;

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

    private String messageReadersKey(Long messageId) {
        return "chat:readers:" + messageId;
    }

    // 마지막 읽은 메시지 ID 저장
    public void updateLastRead(String roomType, Long roomId, Long userId, Long messageId) {
        String lastReadKey = userLastReadKey(roomType, roomId, userId);

        // 개인의 last read ID 저장
        redisTemplate.opsForValue().set(lastReadKey, messageId);

        // 메시지를 읽은 유저 목록 SET에 추가
        redisTemplate.opsForSet().add(messageReadersKey(messageId), userId);
    }

    // 유저의 마지막 읽은 메시지 ID 조회
    public Long getLastRead(String roomType, Long roomId, Long userId) {
        Object value = redisTemplate.opsForValue().get(userLastReadKey(roomType, roomId, userId));
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    // 특정 메시지를 읽은 사람 수
    public int getReadCount(Long messageId) {
        Long size = redisTemplate.opsForSet().size(messageReadersKey(messageId));
        return size == null ? 0 : size.intValue();
    }

    // 특정 메시지를 읽은 사람인지 체크 (1:1 방에서 필요)
    public boolean hasUserRead(Long messageId, Long userId) {
        Boolean isMember = redisTemplate.opsForSet().isMember(messageReadersKey(messageId), userId);
        return isMember != null && isMember;
    }
}
