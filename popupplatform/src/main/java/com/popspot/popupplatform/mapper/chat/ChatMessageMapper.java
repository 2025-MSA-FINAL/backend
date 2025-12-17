package com.popspot.popupplatform.mapper.chat;


import com.popspot.popupplatform.dto.chat.response.ChatMessageImageRow;
import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ChatMessageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    void insertMessage(ChatMessageRequest request);

    List<ChatMessageResponse> getMessagesByRoom(
            @Param("roomType") String roomType,
            @Param("roomId") Long roomId,
            @Param("lastMessageId") Long lastMessageId,
            @Param("limit") int limit,
            @Param("lastDeletedAt") LocalDateTime lastDeletedAt
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
    Long getSenderIdByMessageId(@Param("cmId") Long cmId);

    void insertImages(
            @Param("cmId") Long cmId,
            @Param("urls") List<String> urls
    );

    List<String> selectImageUrlsByCmId(@Param("cmId") Long cmId);

    List<ChatMessageImageRow> selectImagesByCmIds(
            @Param("cmIds") List<Long> cmIds
    );
}
