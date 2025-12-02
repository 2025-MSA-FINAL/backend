package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.PrivateChatRoom;
import com.popspot.popupplatform.dto.chat.response.ChatRoomSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrivateChatRoomMapper {
    //1:1채팅방생성
    void insertRoom(PrivateChatRoom room);
    //채팅방 엔티티 단순 조회 (내부로직)
    PrivateChatRoom findById(Long pcrId);
    //기존채팅방 존재여부확인
    PrivateChatRoom findActiveRoomByUsers(
      @Param("userId1") Long userId1,
      @Param("userId2") Long userId2
    );
    //내가참여한1:1채팅방목록조회
    List<ChatRoomSummaryResponse> findPrivateRoomsForUser(@Param("userId") Long userId);
}
