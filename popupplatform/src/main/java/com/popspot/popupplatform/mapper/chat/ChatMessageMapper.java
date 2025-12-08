package com.popspot.popupplatform.mapper.chat;


import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    void insertMessage(ChatMessageRequest request);

    List<ChatMessageResponse> getMessagesByRoom(
            @Param("roomType") String roomType,
            @Param("roomId") Long roomId,
            @Param("lastMessageId") Long lastMessageId,
            @Param("limit") int limit
    );

    ChatMessageResponse getLatestMessage(
            @Param("roomType") String roomType,
            @Param("roomId") Long roomId
    );

    ChatMessageResponse getMessageById(
            @Param("roomType") String roomType,
            @Param("cmId") Long cmId
    );

    int countUnreadMessages(
            @Param("roomType") String roomType,
            @Param("roomId") Long roomId,
            @Param("lastReadId") Long lastReadId
    );
}
