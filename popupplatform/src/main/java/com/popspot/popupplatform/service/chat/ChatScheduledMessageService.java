package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatScheduledMessage;
import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.dto.chat.response.ScheduledMessageResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ChatErrorCode;
import com.popspot.popupplatform.mapper.chat.ChatScheduledMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatScheduledMessageService {
    private final ChatScheduledMessageMapper chatScheduledMessageMapper;

    //예약 메시지 생성
    @Transactional
    public void createSchedule(
            String roomType,
            Long roomId,
            Long senderId,
            String content,
            LocalDateTime scheduledAt
    ){
        // 과거 시점 예약 방지 (즉시 전송되는 버그 예방)
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약 시간은 미래여야 합니다.");
        }

        // 예약 메시지 도메인 객체 생성
        ChatScheduledMessage msg = new ChatScheduledMessage();
        msg.setRoomType(roomType);
        msg.setRoomId(roomId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setCsmType("TEXT");
        msg.setScheduledAt(scheduledAt);

        // DB에 예약 메시지 저장 (status = PENDING)
        chatScheduledMessageMapper.insert(msg);
    }

    // 예약 메시지를 실제 ChatMessageRequest로 변환
    public ChatMessageRequest toChatMessage(ChatScheduledMessage s) {
        ChatMessageRequest req = new ChatMessageRequest();
        req.setRoomType(s.getRoomType());
        req.setRoomId(s.getRoomId());
        req.setSenderId(s.getSenderId());
        req.setMessageType(s.getCsmType());
        req.setContent(s.getContent());
        return req;
    }

    // 예약 목록 조회
    @Transactional(readOnly = true)
    public List<ScheduledMessageResponse> getMySchedules(
            Long userId,
            String status
    ) {
        List<ChatScheduledMessage> list =
                chatScheduledMessageMapper.findMySchedules(userId, status);

        return list.stream().map(s -> {
            ScheduledMessageResponse res = new ScheduledMessageResponse();
            res.setCsmId(s.getCsmId());
            res.setRoomType(s.getRoomType());
            res.setRoomId(s.getRoomId());
            res.setContent(s.getContent());
            res.setCsmType(s.getCsmType());
            res.setScheduledAt(s.getScheduledAt());
            res.setCsmStatus(s.getCsmStatus());
            res.setCreatedAt(s.getCreatedAt());
            return res;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateSchedule(
            Long csmId,
            Long userId,
            String content,
            LocalDateTime scheduledAt
    ) {
        // 공통 검증
        validatePendingScheduleOrThrow(csmId, userId);

        // 수정할 내용이 없는 경우
        if (content == null && scheduledAt == null) {
            throw new CustomException(ChatErrorCode.SCHEDULE_INVALID_UPDATE);
        }

        // 예약 시간 수정 시 과거 방지
        if (scheduledAt != null &&
                scheduledAt.isBefore(LocalDateTime.now())) {
            throw new CustomException(ChatErrorCode.SCHEDULE_TIME_INVALID);
        }

        chatScheduledMessageMapper.updateSchedule(
                csmId,
                content,
                scheduledAt
        );
    }

    //예약 취소
    @Transactional
    public void cancelSchedule(Long csmId, Long userId) {
        // 공통 검증
        validatePendingScheduleOrThrow(csmId, userId);

        chatScheduledMessageMapper.markAsCanceled(csmId);
    }

    //공통 검증 로직
    private void validatePendingScheduleOrThrow(
            Long csmId,
            Long userId
    ) {
        ChatScheduledMessage msg =
                chatScheduledMessageMapper.findById(csmId);

        if (msg == null) {
            throw new CustomException(ChatErrorCode.SCHEDULE_NOT_FOUND);
        }

        // 본인 예약만 가능
        if (!msg.getSenderId().equals(userId)) {
            throw new CustomException(ChatErrorCode.SCHEDULE_CANCEL_FORBIDDEN);
        }

        // 이미 처리된 예약은 불가
        if (!"PENDING".equals(msg.getCsmStatus())) {
            throw new CustomException(ChatErrorCode.SCHEDULE_ALREADY_PROCESSED);
        }
    }
}