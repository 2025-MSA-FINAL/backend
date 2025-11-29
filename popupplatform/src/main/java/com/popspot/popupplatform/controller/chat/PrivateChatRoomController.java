package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.PrivateChatStartRequest;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.PrivateChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/private")
@Tag(name = "PrivateChat", description = "1:1 채팅 API")
public class PrivateChatRoomController {
    private final PrivateChatRoomService privateChatRoomService;

    //1:1 채팅방 시작 API
    @PostMapping("/start")
    @Operation(
            summary = "1:1 채팅 시작",
            description = """
                    상대 유저와 1:1 채팅을 시작합니다.
                    삭제되지 않은 기존 방이 있으면 해당 방을 재사용하고,
                    숨김된 상태라도 숨김을 유지합니다.
                    기존 방이 없으면 새로운 1:1 채팅방을 생성합니다.
                    자기 자신에게도 채팅을 시작할 수 있습니다.
                    """
    )
    public ResponseEntity<Long> startPrivateChat(
            @RequestBody PrivateChatStartRequest req, //채팅시작요청정보 req
            @AuthenticationPrincipal CustomUserDetails user //현재로그인한사용자정보 user
    ) {
        //서비스에서 채팅방생성 및 기존방 조회
        Long roomId = privateChatRoomService.startPrivateChat(user.getUserId(),req.getTargetUserId());
        return ResponseEntity.ok(roomId);
    }
}
