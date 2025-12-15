package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.response.GroupChatParticipantResponse;
import com.popspot.popupplatform.mapper.chat.ChatParticipantMapper;
import com.popspot.popupplatform.mapper.chat.PrivateChatParticipantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReadService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatParticipantMapper chatParticipantMapper;
    private final PrivateChatParticipantMapper privateChatParticipantMapper;

    private String userLastReadKey(String roomType, Long roomId, Long userId) {
        return "chat:read:" + roomType + ":" + roomId + ":" + userId;
    }

    // 마지막 읽은 메시지 ID 저장
    public void updateLastRead(String roomType, Long roomId, Long userId, Long messageId) {

        if ("GROUP".equals(roomType)) {
            chatParticipantMapper.updateLastRead(roomId, userId, messageId);
        } else {
            privateChatParticipantMapper.insertParticipant(roomId, userId);
            privateChatParticipantMapper.updateLastRead(roomId, userId, messageId);
        }

        // Redis는 캐시
        redisTemplate.opsForValue().set(
                userLastReadKey(roomType, roomId, userId),
                messageId.toString()
        );
    }

    // 유저의 마지막 읽은 메시지 ID 조회
    public Long getLastRead(String roomType, Long roomId, Long userId) {

        Object cached = redisTemplate.opsForValue().get(userLastReadKey(roomType, roomId, userId));
        if (cached != null) return Long.parseLong(cached.toString());

        Long dbValue;
        if ("GROUP".equals(roomType)) {
            dbValue = chatParticipantMapper.findParticipants(roomId).stream()
                    .filter(p -> p.getUserId().equals(userId))
                    .map(GroupChatParticipantResponse::getLastReadMessageId)
                    .findFirst()
                    .orElse(0L);
        } else {
            Long v = privateChatParticipantMapper.findLastRead(roomId, userId);
            dbValue = v == null ? 0L : v;
        }

        redisTemplate.opsForValue().set(userLastReadKey(roomType, roomId, userId), dbValue.toString());
        return dbValue;
    }
}
