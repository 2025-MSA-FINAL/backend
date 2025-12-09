package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final ChatReadService chatReadService;

    public ChatMessageResponse saveMessage(ChatMessageRequest req) {

        // 1) INSERT → 자동 생성된 PK(cm_id)가 req.cmId에 채워짐
        chatMessageMapper.insertMessage(req);

        // 2) 생성된 메시지를 DB에서 다시 조회
        ChatMessageResponse saved = chatMessageMapper.getMessageById(
                req.getRoomType(),
                req.getCmId()
        );

        if (saved == null) {
            throw new RuntimeException("메시지 조회 실패");
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

        List<ChatMessageResponse> messages =
                chatMessageMapper.getMessagesByRoom(roomType, roomId, lastMessageId, limit);

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
