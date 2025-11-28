package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatParticipant;
import com.popspot.popupplatform.domain.chat.GroupChatRoom;
import com.popspot.popupplatform.dto.chat.request.CreateGroupChatRoomRequest;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomListResponse;
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
    private final ChatParticipantMapper ParticipantMapper;

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
        ParticipantMapper.insertParticipant(cp);

        //생성된 채팅방 객체 ID 반환
        return room.getGcrId();
    }

    //팝업 스토어 ID로 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<GroupChatRoomListResponse> getRoomsByPopId(Long popId) {
        return roomMapper.findRoomsByPopId(popId);
    }
}
