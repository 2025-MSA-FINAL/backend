package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.mapper.popup.PopupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupStatusScheduler {


    private final PopupMapper popupMapper;

    // 1분마다 실행 (운영 정책에 따라 1시간(0 0 * * * *) 등으로 변경 가능)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void schedulePopupStatusUpdate() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 오픈 처리 (UPCOMING -> ONGOING)
        int openedCount = popupMapper.updateStatusToOngoing(now);

        // 2. 종료 처리 (UPCOMING, ONGOING -> ENDED)
        int endedCount = popupMapper.updateStatusToEnded(now);

        if (openedCount > 0 || endedCount > 0) {
            log.info("[PopupScheduler] 상태 업데이트 완료 | 오픈처리: {}건 | 종료처리: {}건 | 기준시각: {}",
                    openedCount, endedCount, now);
        }
    }

}