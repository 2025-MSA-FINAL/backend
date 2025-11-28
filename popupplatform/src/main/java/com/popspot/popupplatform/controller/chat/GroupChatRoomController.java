package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.CreateGroupChatRoomRequest;
import com.popspot.popupplatform.dto.chat.request.GroupChatJoinRequest;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomListResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.GroupChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/group")
@Tag(name = "GroupChat", description = "그룹 채팅방 관련 API")
public class GroupChatRoomController {
    private final GroupChatRoomService roomService;

    //그룹채팅방 생성 API
    @PostMapping("/create")
    @Operation(
            summary = "그룹 채팅방 생성",
            description = "새로운 그룹 채팅방을 생성하고 방장을 자동으로 참여자로 등록한다."
    )
    public ResponseEntity<Long> createRoom(
            @RequestBody CreateGroupChatRoomRequest req, //채팅방생성정보 req
            @AuthenticationPrincipal CustomUserDetails user //현재 로그인한 사용자정보 user
            ) {
        //서비스에서 채팅방생성요청, 로그인 인증된 사용자ID를 방장으로 설정
        Long newRoomId = roomService.createRoom(req, user.getUserId());

        //생성된 채팅방 ID 응답
        return ResponseEntity.ok(newRoomId);
    }

    //그룹채팅방 목록 조회 API
    @GetMapping("/list")
    @Operation(
            summary = "특정 팝업의 그룹 채팅방 목록 조회",
            description = "popId를 기준으로 생성된 그룹 채팅방들을 조회합니다."
    )
    public ResponseEntity<List<GroupChatRoomListResponse>> getRoomList(
            @RequestParam Long popId
    ) {
        //서비스에서 popID에 대한 채팅방 목록 조회
        List<GroupChatRoomListResponse> roomList = roomService.getRoomsByPopId(popId);
        //조회된 채팅방 목록 응답
        return ResponseEntity.ok(roomList);
    }

    //그룹채팅방 참여 API
    @PostMapping("/join")
    @Operation(
            summary = "그룹 채팅방 참여",
            description = "유저가 그룹채팅방에 참여합니다."
    )
    public ResponseEntity<String> joinRoom(
            @RequestBody GroupChatJoinRequest req, //채팅방참여요청정보 req - 그룹채팅방ID
            @AuthenticationPrincipal CustomUserDetails user //현재 로그인한 사용자정보 user
    ) {
        //서비스에서 채팅방 참여 처리
        roomService.joinRoom(req.getGcrId(), user.getUserId());
        return ResponseEntity.ok("참여 완료");
    }
}
