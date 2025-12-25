package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.mapper.reservation.ReservationPaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationPaymentCleanupScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReservationPaymentMapper reservationPaymentMapper;

    // UserReservationServiceImpl과 동일 키 사용 중
    private static final String HOLD_EXPIRY_ZSET = "hold:expiry";

    // "10분 안에 결제 안되면" 기준
    private static final Duration EXPIRE_AFTER = Duration.ofMinutes(10);

    // 한 번에 너무 많이 처리하지 않게 배치 제한
    private static final int BATCH_SIZE = 200;

    /**
     * N초마다 만료 HOLD 정리 + PENDING payment 삭제 + Redis 재고 원복
     *
     * fixedDelay: 이전 실행 끝난 뒤 Nms 후 실행
     * 필요하면 application.yml로 조절 가능하게 바꿔도 됨.
     */
    @Scheduled(fixedDelayString = "${reservation.cleanup.fixedDelayMs:5000}")
    public void cleanupExpiredPendingPayments() {
        long nowMillis = System.currentTimeMillis();

        // 1) 만료된 holdId들(= score <= now) 조회
        Set<String> expiredHoldIds = stringRedisTemplate.opsForZSet()
                .rangeByScore(HOLD_EXPIRY_ZSET, 0, nowMillis, 0, BATCH_SIZE);

        if (expiredHoldIds == null || expiredHoldIds.isEmpty()) return;

        Instant cutoff = Instant.ofEpochMilli(nowMillis).minus(EXPIRE_AFTER);

        for (String holdId : expiredHoldIds) {
            try {
                cleanupOne(holdId, cutoff);
            } catch (Exception e) {
                // 한 건 실패로 전체 스케줄러가 죽지 않게
                log.warn("[cleanup] failed holdId={}", holdId, e);
            }
        }
    }

    /**
     * 핵심: DB에서 "PENDING & 10분 지난 건"을 조건부로 삭제(또는 변경)하고,
     * 삭제가 성공한 경우에만 재고 원복한다. (멱등/중복원복 방지)
     */
    @Transactional
    protected void cleanupOne(String holdId, Instant cutoff) {
        if (holdId == null || holdId.isBlank()) return;

        String merchantUid = "hold-" + holdId;

        // 2) DB에서 아직 PENDING이고 10분 지난 것만 삭제 (성공하면 1)
        int deleted = reservationPaymentMapper.deleteExpiredPendingByMerchantUid(
                merchantUid,
                Timestamp.from(cutoff)
        );

        // 3) Redis/인덱스 정리는 어차피 진행 (쓸데없는 zset 적체 방지)
        try {
            if (deleted == 1) {
                // 삭제 성공한 경우에만 재고 원복
                String metaKey = "holdmeta:" + holdId;
                Map<Object, Object> meta = stringRedisTemplate.opsForHash().entries(metaKey);

                if (meta != null && !meta.isEmpty()) {
                    String invKey = String.valueOf(meta.get("invKey"));
                    int people = Integer.parseInt(String.valueOf(meta.get("people")));

                    if (invKey != null && !invKey.isBlank() && people > 0) {
                        stringRedisTemplate.opsForValue().increment(invKey, people);
                        log.info("[cleanup] restored inventory invKey={}, people={}, holdId={}", invKey, people, holdId);
                    }
                }

                // 실제 hold도 같이 삭제
                stringRedisTemplate.delete("hold:" + holdId);
                stringRedisTemplate.delete("holdmeta:" + holdId);
            }
        } finally {
            // 4) zset 인덱스에서는 무조건 제거
            stringRedisTemplate.opsForZSet().remove(HOLD_EXPIRY_ZSET, holdId);
        }
    }
}
