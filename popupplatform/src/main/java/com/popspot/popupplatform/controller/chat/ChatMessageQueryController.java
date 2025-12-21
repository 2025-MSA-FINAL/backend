package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageListResponse;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatParticipantResponse;
import com.popspot.popupplatform.dto.global.UploadResultDto;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.global.service.ObjectStorageService;
import com.popspot.popupplatform.global.utils.HeicConverter;
import com.popspot.popupplatform.mapper.chat.ChatParticipantMapper;
import com.popspot.popupplatform.service.chat.ChatMessageService;
import com.popspot.popupplatform.service.chat.ChatReadService;
import com.popspot.popupplatform.service.chat.PrivateChatRoomService;
import com.popspot.popupplatform.service.chat.ai.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages")
public class ChatMessageQueryController {

    private final ChatMessageService chatMessageService;
    private final ChatReadService chatReadService;
    private final ChatParticipantMapper participantMapper;
    private final PrivateChatRoomService privateChatRoomService;
    private final ObjectStorageService objectStorageService;
    private final AiChatService aiChatService;
    /**
     * 채팅 메시지 불러오기 API
     * 예)
     * /api/chat/messages?roomType=GROUP&roomId=1&limit=20
     * /api/chat/messages?roomType=GROUP&roomId=1&lastMessageId=100&limit=20
     */
    @GetMapping
    public ResponseEntity<ChatMessageListResponse> getMessages(
            @RequestParam String roomType,
            @RequestParam Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        //채팅 메시지 조회 참여자 권한 체크
        if ("GROUP".equals(roomType)) {
            Integer exists = participantMapper.exists(roomId, userId);
            if (exists == null || exists == 0) {
                throw new CustomException(ChatErrorCode.NOT_JOINED_ROOM);
            }
        }

        List<ChatMessageResponse> messages =
                chatMessageService.getMessages(roomType, roomId, lastMessageId, limit, userId);

        Long lastReadId = chatReadService.getLastRead(roomType, roomId, userId);

        Long otherLastReadId = 0L;
        if ("PRIVATE".equals(roomType)) {
            Long otherUserId = privateChatRoomService.getOtherUserId(roomId, userId);
            otherLastReadId = chatReadService.getLastRead("PRIVATE", roomId, otherUserId);
        }
        List<GroupChatParticipantResponse> participants = null;
        if ("GROUP".equals(roomType)) {
            participants = participantMapper.findParticipants(roomId, userId);
        }

        return ResponseEntity.ok(
                new ChatMessageListResponse(
                        messages,
                        lastReadId,
                        otherLastReadId,
                        participants
                )
        );
    }

    @PostMapping("/images")
    public ResponseEntity<ChatMessageResponse> uploadImages(
            @RequestParam String roomType,
            @RequestParam Long roomId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam String clientMessageKey,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        try {
            Long userId = user.getUserId();

            // 이미지 업로드
            List<String> urls = images.stream().map(file -> {
                try {
                    String contentType = file.getContentType();
                    String originalName = file.getOriginalFilename();

                    boolean isHeic =
                            (contentType != null && contentType.contains("heic")) ||
                                    (originalName != null && originalName.toLowerCase().endsWith(".heic"));

                    // 🔥 HEIC → JPG 변환
                    if (isHeic) {
                        File tempHeic = File.createTempFile("upload-", ".heic");
                        file.transferTo(tempHeic);

                        File jpg = HeicConverter.convertHeicToJpg(tempHeic);

                        byte[] bytes = Files.readAllBytes(jpg.toPath());

                        return objectStorageService.uploadBytes(
                                "chat/user",
                                bytes,
                                "image/jpeg",
                                "jpg"
                        ).getUrl();
                    }

                    // 일반 이미지
                    return objectStorageService.upload("chat/user", file).getUrl();

                } catch (Exception e) {
                    throw new RuntimeException("이미지 처리 실패", e);
                }
            }).toList();


            // 메시지 생성
            ChatMessageRequest req = new ChatMessageRequest();
            req.setRoomType(roomType);
            req.setRoomId(roomId);
            req.setSenderId(userId);
            req.setMessageType("IMAGE");
            req.setContent("[IMAGE]");
            req.setImageUrls(urls);
            req.setClientMessageKey(clientMessageKey);

            // 저장 + Redis publish
            ChatMessageResponse saved = chatMessageService.saveMessage(req);
            saved.setAiMode("PURE_LLM");
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            throw new RuntimeException("이미지 메시지 업로드 실패", e);
        }
    }

    @PostMapping("/pure-llm")
    public ResponseEntity<ChatMessageResponse> pureLlmReply(
            @RequestBody ChatMessageRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();

        //  PRIVATE 방 참여자 검증
        privateChatRoomService.validateParticipant(
                req.getRoomId(), userId);

        String reply =
                aiChatService.getPureLlmReply(req.getContent());

        req.setSenderId(20251212L);
        req.setMessageType("TEXT");
        req.setContent(reply);

        ChatMessageResponse saved =
                chatMessageService.saveMessage(req);

        saved.setAiMode("PURE_LLM");

        return ResponseEntity.ok(saved);
    }
}
