package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminChatRoomDTO;
import com.popspot.popupplatform.dto.admin.AdminChatRoomStatsDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.admin.AdminChatRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminChatRoomServiceImpl implements AdminChatRoomService {

    private final AdminChatRoomMapper chatRoomMapper;

    @Override
    public AdminChatRoomStatsDTO getChatRoomStats() {
        return chatRoomMapper.getChatRoomStats();
    }

    @Override
    public PageDTO<AdminChatRoomDTO> getChatRoomList(Boolean isDeleted, PageRequestDTO pageRequest) {
        List<AdminChatRoomDTO> chatRooms = chatRoomMapper.getChatRoomList(
                isDeleted,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        long totalCount = chatRoomMapper.getChatRoomCount(isDeleted);

        return new PageDTO<>(
                chatRooms,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalCount
        );
    }

    @Override
    public AdminChatRoomDTO getChatRoomDetail(Long chatId) {
        return chatRoomMapper.getChatRoomDetail(chatId);
    }

    @Override
    @Transactional
    public boolean deleteChatRoom(Long chatId) {
        int result = chatRoomMapper.deleteChatRoom(chatId);
        return result > 0;
    }

    @Override
    public PageDTO<AdminChatRoomDTO> searchChatRooms(String keyword, PageRequestDTO pageRequest) {
        List<AdminChatRoomDTO> chatRooms = chatRoomMapper.searchChatRooms(
                keyword,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        long totalCount = chatRoomMapper.countSearchChatRooms(keyword);

        return new PageDTO<>(
                chatRooms,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalCount
        );
    }
}
