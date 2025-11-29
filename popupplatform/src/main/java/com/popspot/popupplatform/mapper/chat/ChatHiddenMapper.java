package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatHidden;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

@Mapper
public interface ChatHiddenMapper {
    //특정유저userId의 특정방chRoomId 숨김상태조회
    ChatHidden findHidden(
            @Param("chType") String chType,
            @Param("chRoomId") Long chRoomId,
            @Param("userId") Long userId
    );
    //숨김상태수정(숨김/숨김해제)
    void updateHiddenFlag(
      @Param("chType") String chType,
      @Param("chRoomId") Long chRoomId,
      @Param("userId") Long userId,
      @Param("chIsHidden") Boolean chIsHidden
    );
    //숨김상태저장
    void insertHidden (ChatHidden chatHidden);
}
