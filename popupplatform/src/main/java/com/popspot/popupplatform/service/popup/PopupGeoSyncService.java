package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.mapper.popup.PopupMapper; // MySQL
import com.popspot.popupplatform.mapper.postgres.PopupGeoMapper; // Postgres
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupGeoSyncService {

    private final PopupMapper popupMapper;       // MySQL
    private final PopupGeoMapper popupGeoMapper; // Postgres

    // 한 번에 가져올 사이즈 (30만이면 1000~5000 권장)
    private static final int PAGE_SIZE = 1000;

    /**
     * [배치] MySQL -> Postgres 전체 동기화
     */
    @Transactional(readOnly = true) // MySQL read
    public long syncAllFromMySqlToPostgres() {
        long total = 0;
        long lastId = 0;

        while (true) {
            List<PopupStore> batch = popupMapper.selectPopupsForGeoSync(lastId, PAGE_SIZE);
            if (batch.isEmpty()) break;

            // Postgres upsert (별도 트랜잭션/DB라 2PC는 아님. 실패 시 로그로 확인)
            popupGeoMapper.bulkUpsertPopupGeo(batch);

            lastId = batch.get(batch.size() - 1).getPopId();
            total += batch.size();

            log.info("[PopupGeoSync] synced batch. lastId={}, batchSize={}, total={}",
                    lastId, batch.size(), total);
        }

        log.info("[PopupGeoSync] DONE. total={}", total);
        return total;
    }

    /**
     * [단건] MySQL -> Postgres 동기화
     *
     * - Admin / Manager 에서 팝업 승인/수정 등 상태 변경 후 호출
     * - MySQL의 POPUPSTORE를 읽어서 Postgres popupstore_geo에 upsert
     */
    @Transactional(readOnly = true) // MySQL read
    public void syncPopup(Long popId) {
        if (popId == null) {
            log.warn("[PopupGeoSync] syncPopup called with null popId");
            return;
        }

        List<Long> ids = Collections.singletonList(popId);
        List<PopupStore> popups = popupMapper.selectPopupStoresForGeoByIds(ids);

        if (popups.isEmpty()) {
            // 여기서는 "삭제되었을 가능성" 정도만 로그 남기고 종료
            // 실제 삭제 반영은 softDeletePopup(popId)에서 처리
            log.warn("[PopupGeoSync] syncPopup - popup not found in MySQL. popId={}", popId);
            return;
        }

        PopupStore popup = popups.get(0);

        // Postgres upsert
        popupGeoMapper.upsertPopupGeo(popup);

        log.info("[PopupGeoSync] syncPopup - upserted into Postgres. popId={}", popId);
    }

    /**
     * [단건] Postgres soft delete 반영
     *
     * - MySQL에서 pop_is_deleted = TRUE로 바뀌는 시점에 호출
     *   (Admin 삭제, Manager 삭제 등)
     */
    public void softDeletePopup(Long popId) {
        if (popId == null) {
            log.warn("[PopupGeoSync] softDeletePopup called with null popId");
            return;
        }

        popupGeoMapper.softDeletePopupGeo(popId);
        log.info("[PopupGeoSync] softDeletePopup - marked as deleted in Postgres. popId={}", popId);
    }
}
