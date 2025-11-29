package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.CreateGroupChatRoomRequest;
import com.popspot.popupplatform.dto.chat.request.GroupChatJoinRequest;
import com.popspot.popupplatform.dto.chat.request.UpdateGroupChatRoomRequest;
import com.popspot.popupplatform.dto.chat.response.GroupChatParticipantResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomDetailResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomListResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.GroupChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

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

    //그룹채팅방 수정 API
    @PatchMapping("/update/{gcrId}")
    @Operation(
            summary = "그룹 채팅방 수정",
            description = """
                    입장 조건(성별, 최소/최대 연령)은 채팅방 생성 시에만 설정 가능하며 수정할 수 없습니다.
                    현재 참여자 수보다 작은 최대 인원으로는 설정할 수 없습니다.
                    """
    )
    public ResponseEntity<String> updateRoom (
            @PathVariable Long gcrId, //수정할 그룹채팅방ID
            @RequestBody UpdateGroupChatRoomRequest req, //채팅방수정정보 req
            @AuthenticationPrincipal CustomUserDetails user //현재 로그인한 사용자정보 user
    ) {
        //서비스에서 채팅방 수정 처리
        roomService.updateRoom(gcrId, user.getUserId(), req);
        return ResponseEntity.ok("수정 완료");
    }

    //그룹채팅방 삭제 API
    @DeleteMapping("/delete/{gcrId}")
    @Operation(summary = "그룹 채팅방 삭제", description = "방장이 채팅방을 삭제(비활성화)합니다.")
    public ResponseEntity<String> deleteRoom (
            @PathVariable Long gcrId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        //서비스에서 채팅방 삭제 처리
        roomService.deleteRoom(gcrId, user.getUserId());
        return ResponseEntity.ok("삭제 완료");
    }

    //그룹채팅방 상세조회 API
    @GetMapping("/{gcrId}")
    @Operation(
            summary = "그룹 채팅방 상세 조회",
            description = "채팅방의 제목, 설명, 인원수, 입장 조건 등 기본 정보를 조회합니다."
    )
    public ResponseEntity<GroupChatRoomDetailResponse> getRoomDetail(
            @PathVariable Long gcrId
    ) {
        //서비스에서 채팅방 상세조회
        GroupChatRoomDetailResponse detail = roomService.getRoomDetail(gcrId);
        return ResponseEntity.ok(detail);
    }

    //그룹채팅방 참여자 목록 조회 API
    @GetMapping("/{gcrId}/participants")
    @Operation(
            summary = "그룹 채팅방 참여자 목록 조회",
            description = "해당 그룹 채팅방에 참여 중인 사용자들의 목록을 반환합니다."
    )
    public ResponseEntity<List<GroupChatParticipantResponse>> getParticipants (
            @PathVariable Long gcrId
    ) {
        //서비스에서 참여자 목록 조회
        List<GroupChatParticipantResponse> participants = roomService.getParticipants(gcrId);
        return ResponseEntity.ok(participants);
    }

    //그룹채팅방 나가기 API
    @DeleteMapping("/leave/{gcrId}")
    @Operation(
            summary = "그룹 채팅방 나가기",
            description = "현재 로그인한 사용자가 해당 그룹 채팅방에서 나갑니다. 방장은 나갈 수 없습니다."
    )
    public ResponseEntity<String> leaveRoom (
            @PathVariable Long gcrId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        //서비스에서 참여자 나가기 처리
        roomService.leaveRoom(gcrId, user.getUserId());
        return ResponseEntity.ok("채팅방 나가기 완료");
    }
}
