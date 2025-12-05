package com.popspot.popupplatform.controller.chat;


import com.popspot.popupplatform.domain.chat.ChatMessage;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.service.chat.ChatMessageService;
import com.popspot.popupplatform.service.chat.PrivateChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final PrivateChatRoomService privateChatRoomService;

    //채팅방메세지조회API
    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @RequestParam String roomType, //채팅방유형
            @RequestParam Long roomId, //채팅방ID
            @RequestParam Long userId //보낸UserId
    ) {
        //1대1이면 last_deleted_at 적용(나중에 그룹채팅도 추가해야함)
        String minCreatedAt = null;

        if("PRIVATE".equals(roomType)) {
            LocalDateTime deletedAt = privateChatRoomService.getLastDeletedAt(userId,roomId);
            //삭제한 일이 있으면 마지막 삭제한날짜 가져오기
            if(deletedAt!=null) {
                minCreatedAt = deletedAt.toString();
            }
        }

        //메세지 삭제 또는 나가기 유무 포함한 채팅방 메세지 목록 조회 messages에 저장
        List<ChatMessage> messages = chatMessageService.getMessages(roomType, roomId, minCreatedAt);
        //변환된 DTO를 리스트로 처리
        List<ChatMessageResponse> response = messages.stream()
                .map(chatMessageService::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
