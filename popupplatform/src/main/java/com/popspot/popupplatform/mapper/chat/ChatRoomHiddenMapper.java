package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatRoomHidden;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatRoomHiddenMapper {
    //특정유저userId의 특정방chRoomId 숨김상태조회
    ChatRoomHidden findHidden(
            @Param("crhType") String crhType,
            @Param("crhRoomId") Long crhRoomId,
            @Param("userId") Long userId
    );
    //숨김상태수정(숨김/숨김해제)
    void updateHiddenFlag(
      @Param("crhType") String crhType,
      @Param("crhRoomId") Long crhRoomId,
      @Param("userId") Long userId,
      @Param("crhIsHidden") Boolean crhIsHidden
    );
    //숨김상태저장
    void insertHidden (ChatRoomHidden chatHidden);
    //채팅방 숨김 목록 조회
    List<ChatRoomHidden> findAllHiddenByUser(@Param("userId") Long userId);
}
