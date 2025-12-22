package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.domain.chat.ChatScheduledMessage;
import com.popspot.popupplatform.dto.chat.request.ChatMessageRequest;
import com.popspot.popupplatform.mapper.chat.ChatScheduledMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatScheduledExecutor {
    private final ChatScheduledMessageMapper scheduledMapper;
    private final ChatScheduledMessageService scheduledService;
    private final ChatMessageService chatMessageService;


    // 예약 메시지 실행 스케줄러
    /** 동작 흐름:
     * 1. 현재 시각 기준으로 전송 가능한 예약 메시지 조회
     * 2. 예약 메시지를 ChatMessageRequest 로 변환
     * 3. 기존 saveMessage() 호출 → 일반 메시지처럼 처리
     * 4. 예약 메시지 상태를 SENT 로 변경
     */
    @Scheduled(fixedDelay = 5000) // 5초
    @Transactional
    public void execute() {
        //현재 시각 이전이며 PENDING 상태인 예약 메시지 조회
        List<ChatScheduledMessage> targets =
                scheduledMapper.findExecutableMessages(LocalDateTime.now());


        for (ChatScheduledMessage s : targets) {
            try {
                // 조회된 예약 메시지들을 하나씩 처리
                ChatMessageRequest req =
                        scheduledService.toChatMessage(s);

                // 기존 메시지 저장 로직 재사용
                // → Redis publish / WebSocket / 읽음 처리 자동 수행
                chatMessageService.saveMessage(req);
                // 예약 메시지를 전송 완료 상태로 변경
                scheduledMapper.markAsSent(s.getCsmId());

            } catch (Exception e) {
                // 한 건 실패해도 다른 예약 메시지 처리에 영향 없도록 처리
                e.printStackTrace();
            }
        }
    }
}
