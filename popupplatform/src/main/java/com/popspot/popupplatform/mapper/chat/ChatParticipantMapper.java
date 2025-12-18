package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatParticipant;
import com.popspot.popupplatform.dto.chat.response.GroupChatParticipantResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatParticipantMapper {
    //채팅방참여자입장
    void insertParticipant(ChatParticipant participant);
    //Join시 중복참여체크
    Integer exists(@Param("gcrId") Long gcrId, @Param("userId") Long userId);
    //현재참여자인원
    Integer countParticipants(Long gcrId);
    //채팅방 참여자목록
    List<GroupChatParticipantResponse> findParticipants(
            @Param("gcrId") Long gcrId,
            @Param("currentUserId") Long currentUserId
    );
    //채팅방 나가기
    void deleteParticipant(@Param("gcrId") Long gcrId, @Param("userId") Long userId);
    //읽음표시 업데이트
    void updateLastRead(
            @Param("gcrId") Long gcrId,
            @Param("userId") Long userId,
            @Param("cmId") Long cmId
    );
    //마지막읽음유저 찾기
    Long findLastRead(
            @Param("gcrId") Long gcrId,
            @Param("userId") Long userId
    );
}
