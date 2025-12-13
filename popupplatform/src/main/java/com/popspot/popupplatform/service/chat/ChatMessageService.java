package com.popspot.popupplatform.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.global.redis.RedisPublisher;
import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final PrivateChatRoomService privateChatRoomService;
    private final ChatReadService chatReadService;
    private final AiChatService aiChatService;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;

    // ===============================
    // 🔥 일반 메시지 저장 → Redis publish
    // ===============================
    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageRequest req) {

        // 1) DB 저장
        chatMessageMapper.insertMessage(req);

        // 2) 저장된 메시지 조회
        ChatMessageResponse saved =
                chatMessageMapper.getMessageById(req.getRoomType(), req.getCmId());

        if (saved == null) {
            throw new RuntimeException("메시지 조회 실패");
        }

        saved.setClientMessageKey(req.getClientMessageKey());

        // 3) PRIVATE 채팅이면 방 복구 + AI 여부 확인
        if ("PRIVATE".equals(req.getRoomType())) {

            Long senderId = req.getSenderId();
            Long otherUserId = privateChatRoomService.getOtherUserId(req.getRoomId(), senderId);

            privateChatRoomService.restorePrivateRoomOnNewMessage(senderId, req.getRoomId()); //AI 챗봇 삭제시 필요
            privateChatRoomService.restorePrivateRoomOnNewMessage(otherUserId, req.getRoomId());

            if (otherUserId.equals(20251212L)) {
                asyncAiReply(req);
            }
        }

        // 4) Redis publish (⭐ 단일 출구 ⭐)
        publishMessage(saved);

        return saved;
    }

    // ===============================
    // 🔥 AI 응답 비동기 처리 (Redis로만 publish)
    // ===============================
    @Async
    public void asyncAiReply(ChatMessageRequest userMsg) {
        // AI 타이핑 시작
        publishTyping(
                "TYPING_START",
                "PRIVATE",
                userMsg.getRoomId(),
                20251212L,
                "POPBOT"
        );
        try {
        String aiReply = aiChatService.getAiReply(userMsg.getContent());

        // 타이핑 시간 보장
        try {
            Thread.sleep(Math.min(1500, aiReply.length() * 30L));
        } catch (InterruptedException ignored) {}

        ChatMessageRequest aiMessage = new ChatMessageRequest();
        aiMessage.setRoomType("PRIVATE");
        aiMessage.setRoomId(userMsg.getRoomId());
        aiMessage.setSenderId(20251212L);
        aiMessage.setMessageType("TEXT");
        aiMessage.setContent(aiReply);
        aiMessage.setClientMessageKey(UUID.randomUUID().toString());

            // DB 저장
        chatMessageMapper.insertMessage(aiMessage);

        // 저장된 AI 메시지 조회
        ChatMessageResponse saved =
                chatMessageMapper.getMessageById("PRIVATE", aiMessage.getCmId());

        saved.setClientMessageKey(aiMessage.getClientMessageKey());
        // Redis publish AI 메시지 publish
        publishMessage(saved);
    } catch (Exception e) {
        e.printStackTrace();
    } finally { // AI 타이핑 종료
            publishTyping(
                    "TYPING_STOP",
                    "PRIVATE",
                    userMsg.getRoomId(),
                    20251212L,
                    "POPBOT"
            );
        }
    }

    // ===============================
    // Redis Typing
    // ===============================
    private void publishTyping(String type, String roomType, Long roomId, Long senderId, String nickname) {
        try {
            redisPublisher.publish(
                    "chat-room-" + roomType + "-" + roomId,
                    objectMapper.writeValueAsString(
                            Map.of(
                                    "type", type,
                                    "roomType", roomType,
                                    "roomId", roomId,
                                    "senderId", senderId,
                                    "senderNickname", nickname
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ===============================
    // Redis Message
    // ===============================
    private void publishMessage(ChatMessageResponse msg) {
        try {
            redisPublisher.publish(
                    "chat-room-" + msg.getRoomType() + "-" + msg.getRoomId(),
                    objectMapper.writeValueAsString(
                            Map.of(
                                    "type", "MESSAGE",
                                    "payload", msg
                            )
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ===============================
    // 메시지 조회 (변경 없음)
    // ===============================
    public List<ChatMessageResponse> getMessages(
            String roomType,
            Long roomId,
            Long lastMessageId,
            int limit,
            Long userId
    ) {

        LocalDateTime lastDeletedAt = null;

        if (roomType.equals("PRIVATE")) {
            lastDeletedAt = privateChatRoomService.getLastDeletedAt(userId, roomId);
        }

        List<ChatMessageResponse> messages =
                chatMessageMapper.getMessagesByRoom(
                        roomType,
                        roomId,
                        lastMessageId,
                        limit,
                        lastDeletedAt
                );

        Long lastReadId = chatReadService.getLastRead(roomType, roomId, userId);

        boolean separatorInserted = false;

        for (ChatMessageResponse msg : messages) {

            msg.setReadCount(chatReadService.getReadCount(msg.getCmId()));
            msg.setIsRead(msg.getCmId() <= lastReadId);

            if (!separatorInserted && msg.getCmId() > lastReadId) {
                msg.setUnreadSeparator(true);
                separatorInserted = true;
            }
        }

        return messages;
    }
}
