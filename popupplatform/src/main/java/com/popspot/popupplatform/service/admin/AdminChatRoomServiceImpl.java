package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminChatParticipantDTO;
import com.popspot.popupplatform.dto.admin.AdminChatReportDTO;
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
    public PageDTO<AdminChatRoomDTO> getChatRoomList(Boolean isDeleted, String sort,PageRequestDTO pageRequest) {
        List<AdminChatRoomDTO> chatRooms = chatRoomMapper.getChatRoomList(
                isDeleted,
                sort,
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
    public PageDTO<AdminChatRoomDTO> searchChatRooms(
            String keyword,
            Boolean isDeleted,
            String searchType,
            String sort,
            PageRequestDTO pageRequest) {
        List<AdminChatRoomDTO> chatRooms = chatRoomMapper.searchChatRooms(
                keyword,
                isDeleted,
                searchType,
                sort,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        long totalCount = chatRoomMapper.countSearchChatRooms(
                keyword,
                isDeleted,
                searchType
        );

        return new PageDTO<>(
                chatRooms,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalCount
        );
    }

    @Override
    public List<AdminChatParticipantDTO> getChatRoomParticipants(Long chatId) {
        return chatRoomMapper.getChatRoomParticipants(chatId);
    }


    @Override
    @Transactional
    public boolean restoreChatRoom(Long chatId) {
        return chatRoomMapper.restoreChatRoom(chatId) > 0;
    }

    @Override
    public List<AdminChatReportDTO> getChatRoomReports(Long chatId) {
        return chatRoomMapper.getChatRoomReports(chatId);
    }

    @Override
    @Transactional
    public boolean updateChatReportStatus(Long reportId, String status) {
        return chatRoomMapper.updateChatReportStatus(reportId, status) > 0;
    }




}
