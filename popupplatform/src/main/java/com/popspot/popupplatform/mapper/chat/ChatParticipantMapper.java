package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatParticipant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatParticipantMapper {
    //채팅방참여자입장
    void insertParticipant(ChatParticipant participant);
}
