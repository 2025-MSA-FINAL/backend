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
}
