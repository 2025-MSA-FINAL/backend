package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminChatRoomDTO;
import com.popspot.popupplatform.dto.admin.AdminChatRoomStatsDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

public interface AdminChatRoomService {
    AdminChatRoomStatsDTO getChatRoomStats();
    PageDTO<AdminChatRoomDTO> getChatRoomList(Boolean isDeleted, PageRequestDTO pageRequest);
    AdminChatRoomDTO getChatRoomDetail(Long chatId);
    boolean deleteChatRoom(Long chatId);
    PageDTO<AdminChatRoomDTO> searchChatRooms(String keyword, PageRequestDTO pageRequest);
}
