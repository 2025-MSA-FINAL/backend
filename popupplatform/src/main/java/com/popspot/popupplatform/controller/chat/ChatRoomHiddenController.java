package com.popspot.popupplatform.controller.chat;


import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.ChatRoomHiddenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/hidden")
public class ChatRoomHiddenController {
    private final ChatRoomHiddenService chatRoomHiddenService;
    //채팅방 숨김처리
    @Operation(summary = "채팅방 숨김 처리", description = "1:1 또는 그룹 채팅방을 목록에서 숨김 처리합니다.")
    @PostMapping("/hide")
    public ResponseEntity<String> hide(
            @RequestParam String crhType, //PRIVATE, GROUP
            @RequestParam Long crhRoomId, //채팅방번호
            @AuthenticationPrincipal CustomUserDetails user //숨김 설정한 유저ID(로그인)
    ) {
        chatRoomHiddenService.hideRoom(crhType,crhRoomId,user.getUserId());
        return ResponseEntity.ok("채팅방 숨김 처리 완료");
    }
    //채팅방 숨김해제
    @Operation(summary = "채팅방 숨김 해제", description = "숨김 처리된 채팅방을 목록에 다시 표시합니다.")
    @PostMapping("/unhide")
    public ResponseEntity<String> unhide(
            @RequestParam String crhType, //PRIVATE, GROUP
            @RequestParam Long crhRoomId, //채팅방번호
            @AuthenticationPrincipal CustomUserDetails user //숨김 설정한 유저ID(로그인)
    ) {
        chatRoomHiddenService.unhideRoom(crhType, crhRoomId, user.getUserId());
        return  ResponseEntity.ok("채팅방 숨김 해제 완료");
    }

}
