package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatParticipant;
import com.popspot.popupplatform.domain.chat.GroupChatRoom;
import com.popspot.popupplatform.dto.chat.request.CreateGroupChatRoomRequest;
import com.popspot.popupplatform.dto.chat.request.UpdateGroupChatRoomRequest;
import com.popspot.popupplatform.dto.chat.response.GroupChatParticipantResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomDetailResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomListResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatParticipantMapper;
import com.popspot.popupplatform.mapper.chat.GroupChatRoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupChatRoomService {
    private final GroupChatRoomMapper roomMapper;
    private final ChatParticipantMapper participantMapper;

    //공통검증메서드
    private GroupChatRoom validateRoomOwnership(Long gcrId, Long userId) {
        GroupChatRoom room = roomMapper.findById(gcrId);
        //존재하지 않는 방 수정 불가 버그
        if (room == null) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        //삭제된 방 수정 불가 버그
        if (Boolean.TRUE.equals(room.getGcrIsDeleted())) {
            throw new CustomException(ChatErrorCode.ROOM_ALREADY_DELETED);
        }
        //수정시 방장 권한 확인
        if (!room.getUserId().equals(userId)) {
            throw new CustomException(ChatErrorCode.NOT_ROOM_OWNER);
        }
        return room;
    }
    //채팅방 생성
    //채팅방생성정보 req, 채팅방생성유저(방장) userId
    @Transactional
    public Long createRoom(CreateGroupChatRoomRequest req, Long userId) {
        //채팅방 객체 생성
        GroupChatRoom room = GroupChatRoom.builder()
                .popId(req.getPopId())
                .userId(userId)
                .cmId(0L)
                .gcrTitle(req.getTitle())
                .gcrDescription(req.getDescription())
                .gcrMaxUserCnt(req.getMaxUserCnt())
                .gcrLimitGender(req.getLimitGender())
                .gcrMinAge(req.getMinAge())
                .gcrMaxAge(req.getMaxAge())
                .gcrIsDeleted(false)
                .build();
        //DB저장
        roomMapper.insertRoom(room);

        //방 생성 시 방장은 자동으로 참여자로 추가
        ChatParticipant cp = ChatParticipant.builder()
                .gcrId(room.getGcrId())
                .userId(userId)
                .cmId(0L)
                .build();
        participantMapper.insertParticipant(cp);

        //생성된 채팅방 객체 ID 반환
        return room.getGcrId();
    }
    //팝업 스토어 ID로 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<GroupChatRoomListResponse> getRoomsByPopId(Long popId) {
        return roomMapper.findRoomsByPopId(popId);
    }
    //채팅방 참여
    //참여할 채팅방 gcrId, 참여할 유저 userId
    @Transactional
    public void joinRoom(Long gcrId, Long userId) {
        //참여중인지 확인
        Integer exists = participantMapper.exists(gcrId, userId);
        //만약 참여중이라면 안내문구
        if (exists != null && exists > 0) {
            throw new CustomException(ChatErrorCode.ALREADY_JOINED);
        }
        //참여자 엔티티 생성
        ChatParticipant participant = ChatParticipant.builder()
                .gcrId(gcrId)
                .userId(userId)
                .cmId(0L)
                .build();
        //참여자저장
        participantMapper.insertParticipant(participant);
    }
    //채팅방 수정
    //수정할 채팅방 gcrId, 수정권한을 위한 방장ID userId, 채팅방수정정보 req
    @Transactional
    public void updateRoom(Long gcrId, Long userId, UpdateGroupChatRoomRequest req) {
        GroupChatRoom room = validateRoomOwnership(gcrId, userId);
        //최대 인원 수정시 현재 인원 이상 검증
        if (req.getMaxUserCnt() != null) {
            //채팅방 내 현재인원
            int currentUserCnt = participantMapper.countParticipants(gcrId);
            //수정인원이 현재인원보다 적을 시
            if(req.getMaxUserCnt() < currentUserCnt) {
                throw new CustomException(ChatErrorCode.MAX_USER_UNDERFLOW);
            }
            //정상반영
            room.setGcrMaxUserCnt(req.getMaxUserCnt());
        }
        room.setGcrTitle(req.getTitle());
        room.setGcrDescription(req.getDescription());
        roomMapper.updateRoom(room);
    }
    //채팅방 삭제
    //방장ID userId, 삭제할 채팅방 gcrId
    @Transactional
    public void deleteRoom(Long gcrId, Long userId) {
        GroupChatRoom room = validateRoomOwnership(gcrId, userId);
        roomMapper.deleteRoom(room);
    }
    //채팅방 상세조회
    //조회할 그룹채팅방 gcrId
    @Transactional(readOnly = true)
    public GroupChatRoomDetailResponse getRoomDetail(Long gcrId) {
        GroupChatRoomDetailResponse detail = roomMapper.findRoomDetail(gcrId);
        //조회할 채팅방이 없다면
        if(detail==null) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        return detail;
    }
    //채팅방 참여자 목록 조회
    //조회할 그룹채팅방 gcrId
    @Transactional(readOnly = true)
    public List<GroupChatParticipantResponse> getParticipants(Long gcrId) {
        GroupChatRoom room = roomMapper.findById(gcrId);
        //존재하지 않는 방 불가 버그
        if (room == null) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        //삭제된 방 불가 버그
        if (Boolean.TRUE.equals(room.getGcrIsDeleted())) {
            throw new CustomException(ChatErrorCode.ROOM_ALREADY_DELETED);
        }
        return participantMapper.findParticipants(gcrId);
    }
    //채팅방 나가기
    //나갈 채팅방 gcrId, 나갈 유저 userId
    @Transactional
    public void leaveRoom(Long gcrId, Long userId) {
        GroupChatRoom room = roomMapper.findById(gcrId);
        //존재하지 않는 방 불가 버그
        if (room == null) {
            throw new CustomException(ChatErrorCode.ROOM_NOT_FOUND);
        }
        //방장이 나갈 경우 불가능
        if (room.getUserId().equals(userId)) {
            throw new CustomException(ChatErrorCode.OWNER_CANNOT_LEAVE);
        }
        //참여 여부 체크 후 참여하지 않았을 경우 버그
        Integer exists = participantMapper.exists(gcrId, userId);
        if (exists == null || exists == 0) {
            throw new CustomException(ChatErrorCode.NOT_JOINED_ROOM);
        }
        participantMapper.deleteParticipant(gcrId, userId);
    }
}
