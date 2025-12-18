package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.postgres.PopupGeoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopupStatusScheduler {

    private final PopupMapper popupMapper;          // MySQL
    private final PopupGeoMapper popupGeoMapper;    // Postgres

    /**
     * 팝업 상태 업데이트 + (커밋 성공 시) Postgres geo 테이블에도 상태 반영
     */
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각 실행
    @Transactional
    public void schedulePopupStatusUpdate() {
        LocalDateTime now = LocalDateTime.now();

        // 1) 바뀔 대상 pop_id 먼저 확보 (MySQL 기준)
        List<Long> toOngoingIds = popupMapper.selectPopupIdsToOngoing(now);
        List<Long> toEndedIds   = popupMapper.selectPopupIdsToEnded(now);

        // 2) MySQL 상태 업데이트
        int ongoingCount = 0;
        int endedCount = 0;

        if (!toOngoingIds.isEmpty()) {
            ongoingCount = popupMapper.updateStatusToOngoing(now);
        }
        if (!toEndedIds.isEmpty()) {
            endedCount = popupMapper.updateStatusToEnded(now);
        }

        log.info("[PopupStatusScheduler] MySQL status update done. ongoing={}, ended={}",
                ongoingCount, endedCount);

        // 3) 변경된 pop_id 합치기
        Set<Long> changedSet = new HashSet<>();
        changedSet.addAll(toOngoingIds);
        changedSet.addAll(toEndedIds);

        if (changedSet.isEmpty()) {
            return;
        }

        List<Long> changedIds = new ArrayList<>(changedSet);

        // 4) “커밋 성공 후” Postgres에 반영 (트랜잭션 실패 시 Postgres 건드리면 안됨)
        Runnable syncPostgres = () -> {
            try {
                List<PopupStore> changedPopups = popupMapper.selectPopupStoresForGeoByIds(changedIds);
                if (changedPopups == null || changedPopups.isEmpty()) {
                    log.info("[PopupStatusScheduler] Postgres sync skipped. changedPopups empty.");
                    return;
                }

                popupGeoMapper.bulkUpsertPopupGeo(changedPopups);
                log.info("[PopupStatusScheduler] Postgres geo sync OK. changed={}", changedPopups.size());
            } catch (Exception e) {
                log.warn("[PopupStatusScheduler] Postgres geo sync FAILED. changedIds={}", changedIds, e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    syncPostgres.run();
                }
            });
        } else {
            // 혹시 트랜잭션이 안 잡힌 상황이면 즉시 실행(안전장치)
            syncPostgres.run();
        }
    }
}
