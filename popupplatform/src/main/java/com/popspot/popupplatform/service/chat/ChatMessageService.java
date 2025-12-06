package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatMessage;
import com.popspot.popupplatform.dto.chat.request.ChatMessageSendRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.mapper.chat.ChatMessageMapper;
import com.popspot.popupplatform.mapper.chat.PrivateChatRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;
    private final PrivateChatRoomService privateChatRoomService;
    //Stomp로 들어온 메세지 DB에 저장
    public ChatMessage saveMessage(ChatMessageSendRequest req) {
        // PRIVATE 채팅일 경우 삭제 상태 자동 해제 처리
        if ("PRIVATE".equals(req.getRoomType())) {

            Long pcrId = req.getRoomId();
            Long senderId = req.getSenderId();

            // 내가 삭제 상태였다면 → 나를 복구
            privateChatRoomService.restorePrivateRoomOnNewMessage(senderId, pcrId);

            // 상대방이 삭제 상태였다면 → 상대방 복구
            Long otherUserId = privateChatRoomService.getOtherUserId(pcrId, senderId);
            privateChatRoomService.restorePrivateRoomOnNewMessage(otherUserId, pcrId);
        }
        // DTO를 DB 저장을 위한 ChatMessage 엔티티로 변환
        ChatMessage message = ChatMessage.builder()
                .cmType(req.getRoomType())
                .cmRoomId(req.getRoomId())
                .cmContent(req.getContent())
                .userId(req.getSenderId())
                .cmIsDeleted(false)
                .cmUrl(req.getImgUrl())
                .build();
        // 메시지를 DB에 삽입
        chatMessageMapper.insertChatMessage(message);

        log.debug("Chat message saved: {}", message);
        return message;
    }
    // 채팅방 메세지 목록 조회(메세지 삭제 또는 나가기 유무 판단)
    public List<ChatMessage> getMessages(String roomType, Long roomId, String minCreatedAt) {
        return chatMessageMapper.findMessages(roomType, roomId, minCreatedAt);
    }
    // 클라이언트로 보내줄 응답 DTO 반환
    public ChatMessageResponse toResponse(ChatMessage message) {
        //ChatMessage 엔티티를 클라이언트로 전송할 응답 DTO로 변환
        return ChatMessageResponse.builder()
                .cmId(message.getCmId())
                .roomType(message.getCmType())
                .roomId(message.getCmRoomId())
                .senderId(message.getUserId())
                .content(message.getCmContent())
                .imgUrl(message.getCmUrl())
                .createdAt(message.getCreatedAt())
                .build();
    }

}
