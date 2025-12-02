package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.AdminChatRoomDTO;
import com.popspot.popupplatform.dto.admin.AdminChatRoomStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminChatRoomMapper {
    AdminChatRoomStatsDTO getChatRoomStats();
    List<AdminChatRoomDTO> getChatRoomList(@Param("isDeleted") Boolean isDeleted, @Param("offset") int offset, @Param("size") int size);
    long getChatRoomCount(@Param("isDeleted") Boolean isDeleted);
    AdminChatRoomDTO getChatRoomDetail(@Param("chatId") Long chatId);
    int deleteChatRoom(@Param("chatId") Long chatId);
    List<AdminChatRoomDTO> searchChatRooms(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);
    long countSearchChatRooms(@Param("keyword") String keyword);
}