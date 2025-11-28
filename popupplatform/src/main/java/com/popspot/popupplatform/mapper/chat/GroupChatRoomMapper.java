package com.popspot.popupplatform.mapper.chat;


import com.popspot.popupplatform.domain.chat.GroupChatRoom;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomListResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupChatRoomMapper {
    //그룹채팅방생성
    void insertRoom(GroupChatRoom room);
    //팝업ID에 대한 그룹채팅방 목록
    List<GroupChatRoomListResponse> findRoomsByPopId(Long popId);
    //그룹채팅방ID를 통한 그룹채팅방 상세조회
    GroupChatRoom findById(Long gcrId);
    //그룹채팅방 수정
    void updateRoom(GroupChatRoom room);
    //그룹채팅방 삭제
    void deleteRoom(GroupChatRoom room);
}
