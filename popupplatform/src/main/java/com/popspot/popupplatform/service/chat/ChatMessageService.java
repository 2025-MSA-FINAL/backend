package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final PrivateChatRoomService privateChatRoomService;
    private final ChatReadService chatReadService;

    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageRequest req) {

        // 1) 메시지 INSERT
        chatMessageMapper.insertMessage(req);

        // 2) INSERT 결과 조회
        ChatMessageResponse saved = chatMessageMapper.getMessageById(
                req.getRoomType(),
                req.getCmId()
        );
        if (saved == null) {
            throw new RuntimeException("메시지 조회 실패");
        }

        /* 🔥 3) PRIVATE이면 자동 restore 처리 */
        if ("PRIVATE".equals(req.getRoomType())) {
            Long pcrId = req.getRoomId();
            Long senderId = req.getSenderId();

            // 상대방 userId 조회 (반드시 필요)
            Long otherUserId = privateChatRoomService.getOtherUserId(pcrId, senderId);

            // 만약 상대방이 삭제한 상태였다면 → 즉시 자동 복구
            privateChatRoomService.restorePrivateRoomOnNewMessage(otherUserId, pcrId);
        }

        return saved;
    }


    private String formatTime(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm");
        return time.format(formatter);
    }

    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일");
        return date.format(formatter);
    }

    public List<ChatMessageResponse> getMessages(String roomType, Long roomId, Long lastMessageId, int limit, Long userId) {

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

            int readCount = chatReadService.getReadCount(msg.getCmId());
            msg.setReadCount(readCount);

            boolean isRead = msg.getCmId() <= lastReadId;
            msg.setIsRead(isRead);

            if (!separatorInserted && msg.getCmId() > lastReadId) {
                msg.setUnreadSeparator(true);
                separatorInserted = true;
            }
        }

        return messages;
    }
}
