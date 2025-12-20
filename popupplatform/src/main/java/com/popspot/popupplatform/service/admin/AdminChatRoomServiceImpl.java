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
    public PageDTO<AdminChatRoomDTO> getChatRoomList(Boolean isDeleted, String sort, PageRequestDTO pageRequest) {
        log.info("=== getChatRoomList (검색 없음) ===");
        log.info("isDeleted: {}, sort: {}, page: {}, size: {}", isDeleted, sort, pageRequest.getPage(), pageRequest.getSize());

        List<AdminChatRoomDTO> chatRooms = chatRoomMapper.getChatRoomList(
                isDeleted,
                sort,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        long totalCount = chatRoomMapper.getChatRoomCount(isDeleted);

        log.info("결과: {} / {}", chatRooms.size(), totalCount);

        return new PageDTO<>(
                chatRooms,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalCount
        );
    }

    @Override
    public PageDTO<AdminChatRoomDTO> searchChatRooms(
            String keyword,
            Boolean isDeleted,
            String searchType,
            String sort,
            PageRequestDTO pageRequest) {

        log.info("=== searchChatRooms (검색 있음) ===");
        log.info("keyword: [{}]", keyword);
        log.info("searchType: [{}]", searchType);
        log.info("isDeleted: {}", isDeleted);
        log.info("sort: {}", sort);
        log.info("page: {}, size: {}, offset: {}", pageRequest.getPage(), pageRequest.getSize(), pageRequest.getOffset());

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

        log.info("결과: {} / {}", chatRooms.size(), totalCount);

        // 결과 미리보기
        if (!chatRooms.isEmpty()) {
            log.info("첫 번째 결과 - chatId: {}, popupName: [{}], chatName: [{}], hostName: [{}]",
                    chatRooms.get(0).getChatId(),
                    chatRooms.get(0).getPopupName(),
                    chatRooms.get(0).getChatName(),
                    chatRooms.get(0).getHostUserName());
        }

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