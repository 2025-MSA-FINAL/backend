package com.popspot.popupplatform.mapper.chat;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrivateChatParticipantMapper {
    void insertParticipant(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId
    );

    void updateLastRead(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );

    Long findLastRead(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId
    );

    Integer exists(
            @Param("pcrId") Long pcrId,
            @Param("userId") Long userId
    );
}
