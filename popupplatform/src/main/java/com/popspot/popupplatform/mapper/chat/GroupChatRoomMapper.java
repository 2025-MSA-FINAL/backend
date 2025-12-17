package com.popspot.popupplatform.mapper.chat;


import com.popspot.popupplatform.domain.chat.GroupChatRoom;
import com.popspot.popupplatform.dto.chat.response.ChatRoomSummaryResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomDetailResponse;
import com.popspot.popupplatform.dto.chat.response.GroupChatRoomListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupChatRoomMapper {
    //그룹채팅방생성
    void insertRoom(GroupChatRoom room);
    //팝업ID에 대한 그룹채팅방 목록
    List<GroupChatRoomListResponse> findRoomsByPopId(@Param("popId") Long popId, @Param("userId") Long userId);
    //채팅방 엔티티 단순 조회 (내부로직 )
    GroupChatRoom findById(@Param("gcrId") Long gcrId);
    //그룹채팅방 수정
    void updateRoom(GroupChatRoom room);
    //그룹채팅방 삭제
    void deleteRoom(GroupChatRoom room);
    //채팅방 상세조회(API응답, JOIN)
    GroupChatRoomDetailResponse findRoomDetail(@Param("gcrId") Long gcrId);
    //내가참여한그룹채팅방목록조회
    List<ChatRoomSummaryResponse> findGroupRoomsForUser(@Param("userId") Long userId);
}
