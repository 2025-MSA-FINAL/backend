package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.mapper.popup.PopupMapper; // MySQL
import com.popspot.popupplatform.mapper.postgres.PopupGeoMapper; // Postgres
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupGeoSyncService {

    private final PopupMapper popupMapper;       // MySQL
    private final PopupGeoMapper popupGeoMapper; // Postgres

    // 한 번에 가져올 사이즈 (30만이면 1000~5000 권장)
    private static final int PAGE_SIZE = 1000;

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
}
