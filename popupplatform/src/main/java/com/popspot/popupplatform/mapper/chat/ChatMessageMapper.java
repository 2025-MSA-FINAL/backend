package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    //채팅메세지DB저장
    void insertChatMessage(ChatMessage message);
    //채팅방 메세지 목록 조회
    //minCreatedAt은 사용자가 마지막으로 메시지 기록을 지운 시간(나가기,삭제 등)이며, 이 값을 기준으로 그 이후 메시지만 조회한다.
    //값이 없으면 (null이면) 전체 메시지를 조회한다
    List<ChatMessage> findMessages(
            @Param("roomType") String roomType, //"PRIVATE" OR "GROUP"
            @Param("roomId") Long roomId, //채팅방ID
            @Param("minCreatedAt") String minCreatedAt //last_deleted_at (nullable)
    );
}
