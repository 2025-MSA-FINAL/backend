package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.domain.chat.ChatScheduledMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatScheduledMessageMapper {

    void insert(ChatScheduledMessage msg); // 예약 메시지 저장
    List<ChatScheduledMessage> findExecutableMessages( //전송 가능한 예약 메시지 조회
            @Param("now") LocalDateTime now
    );
    void markAsSent(@Param("csmId") Long csmId); // 예약 메시지 전송 완료 처리
    void markAsCanceled(@Param("csmId") Long csmId); // 예약 메시지 취소 처리
    ChatScheduledMessage findById(@Param("csmId") Long csmId); // 권한체크용 조회
    List<ChatScheduledMessage> findMySchedules( // 예약 목록 조회
            @Param("senderId") Long senderId,
            @Param("status") String status
    );
    void updateSchedule( // 예약 메세지 수정
            @Param("csmId") Long csmId,
            @Param("content") String content,
            @Param("scheduledAt") LocalDateTime scheduledAt
    );
}
