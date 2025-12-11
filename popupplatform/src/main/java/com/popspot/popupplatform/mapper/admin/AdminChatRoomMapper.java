package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.AdminChatReportDTO;
import com.popspot.popupplatform.dto.admin.AdminChatRoomDTO;
import com.popspot.popupplatform.dto.admin.AdminChatRoomStatsDTO;
import com.popspot.popupplatform.dto.admin.AdminChatParticipantDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminChatRoomMapper {

    AdminChatRoomStatsDTO getChatRoomStats();

    List<AdminChatRoomDTO> getChatRoomList(
            @Param("isDeleted") Boolean isDeleted,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size);

    long getChatRoomCount(@Param("isDeleted") Boolean isDeleted);

    AdminChatRoomDTO getChatRoomDetail(@Param("chatId") Long chatId);

    int deleteChatRoom(@Param("chatId") Long chatId);

    List<AdminChatRoomDTO> searchChatRooms(
            @Param("searchKeyword") String keyword,
            @Param("isDeleted") Boolean isDeleted,
            @Param("sort") String sort,
            @Param("searchType") String searchType,
            @Param("offset") int offset,
            @Param("size") int size);

    long countSearchChatRooms(@Param("searchKeyword") String keyword,
                              @Param("isDeleted") Boolean isDeleted,
                              @Param("searchType") String searchType);

    List<AdminChatParticipantDTO> getChatRoomParticipants(@Param("chatId") Long chatId);

    int restoreChatRoom(@Param("chatId") Long chatId);

    List<AdminChatReportDTO> getChatRoomReports(Long chatId);

    int updateChatReportStatus(@Param("reportId") Long reportId,
                               @Param("status") String status);



}