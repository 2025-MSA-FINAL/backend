package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatParticipantMapper {
    //채팅방참여자입장
    void insertParticipant(ChatParticipant participant);

    //Join시 중복참여체크
    Integer exists(@Param("gcrId") Long gcrId, @Param("userId") Long userId);
}
