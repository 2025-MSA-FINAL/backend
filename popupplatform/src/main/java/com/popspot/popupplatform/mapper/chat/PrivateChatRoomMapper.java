package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.PrivateChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrivateChatRoomMapper {
    //1:1채팅방생성
    void insertRoom(PrivateChatRoom room);

    //기존채팅방 존재여부확인
    PrivateChatRoom findActiveRoomByUsers(
      @Param("userId1") Long userId1,
      @Param("userId2") Long userId2
    );
}
