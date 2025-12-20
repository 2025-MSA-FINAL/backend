package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminChatRoomMapper {

    /**
     * 채팅방 통계 조회
     */
    AdminChatRoomStatsDTO getChatRoomStats();

    /**
     * 채팅방 목록 조회 (검색 없음)
     */
    List<AdminChatRoomDTO> getChatRoomList(
            @Param("isDeleted") Boolean isDeleted,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 채팅방 개수 조회 (검색 없음)
     */
    long getChatRoomCount(@Param("isDeleted") Boolean isDeleted);

    /**
     * 채팅방 검색 (searchType 포함)
     */
    List<AdminChatRoomDTO> searchChatRooms(
            @Param("keyword") String keyword,
            @Param("isDeleted") Boolean isDeleted,
            @Param("searchType") String searchType,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 검색 결과 개수 (searchType 포함)
     */
    long countSearchChatRooms(
            @Param("keyword") String keyword,
            @Param("isDeleted") Boolean isDeleted,
            @Param("searchType") String searchType
    );

    /**
     * 채팅방 상세 조회
     */
    AdminChatRoomDTO getChatRoomDetail(@Param("chatId") Long chatId);

    /**
     * 채팅방 삭제 (soft delete)
     */
    int deleteChatRoom(@Param("chatId") Long chatId);

    /**
     * 채팅방 복구
     */
    int restoreChatRoom(@Param("chatId") Long chatId);

    /**
     * 채팅방 참여자 목록
     */
    List<AdminChatParticipantDTO> getChatRoomParticipants(@Param("chatId") Long chatId);

    /**
     * 채팅방 신고 목록
     */
    List<AdminChatReportDTO> getChatRoomReports(@Param("chatId") Long chatId);

    /**
     * 신고 상태 업데이트
     */
    int updateChatReportStatus(
            @Param("reportId") Long reportId,
            @Param("status") String status
    );
}