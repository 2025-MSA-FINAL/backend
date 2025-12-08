package com.popspot.popupplatform.mapper.chat;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatReadMapper {
    Long getLastRead(
            @Param("roomType") String roomType,
            @Param("roomId") Long roomId,
            @Param("userId") Long userId
    );

    void updateLastRead(
            @Param("roomType") String roomType,
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("messageId") Long messageId
    );

    int getReadCount(
            @Param("messageId") Long messageId
    );
}
