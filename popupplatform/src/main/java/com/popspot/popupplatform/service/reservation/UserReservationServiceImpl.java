package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.reservation.UserReservation;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.reservation.ReservationPaymentMapper;
import com.popspot.popupplatform.mapper.reservation.UserReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserReservationServiceImpl implements UserReservationService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReservationPaymentMapper reservationPaymentMapper;
    private final PopupMapper popupMapper;

    // ✅ 추가: USER_RESERVATION insert용 mapper
    private final UserReservationMapper userReservationMapper;

    // HOLD TTL
    private static final Duration HOLD_TTL = Duration.ofMinutes(10);

    // 원복용 meta는 좀 더 길게(hold TTL 지나도 복구 가능하게)
    private static final Duration HOLD_META_TTL = Duration.ofHours(1);

    private static final String HOLD_EXPIRY_ZSET = "hold:expiry";

    // ✅ 원자적 차감을 위한 Lua (remain >= people 이면 DECRBY, 아니면 실패)
    private static final DefaultRedisScript<Long> DECR_IF_ENOUGH_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local key = KEYS[1]
                    local cnt = tonumber(ARGV[1])

                    local v = redis.call('GET', key)
                    if (not v) then
                      return 0
                    end

                    local remain = tonumber(v)
                    if (remain == nil) then
                      return 0
                    end

                    if (remain < cnt) then
                      return 0
                    end

                    redis.call('DECRBY', key, cnt)
                    return 1
                    """,
                    Long.class
            );

    @Override
    public Long createReservationConfirmed(Long popupId, Long slotId, LocalDate date, int people,Long userId) {
        if (popupId == null || slotId == null || date == null) {
            throw new IllegalArgumentException("popupId/slotId/date is required");
        }
        if (people <= 0) {
            throw new IllegalArgumentException("people must be > 0");
        }

        // 1) 재고 키
        String invKey = buildInventoryKey(popupId, date, slotId);

        // 2) Redis Lua로 원자적 차감
        Long ok = stringRedisTemplate.execute(
                DECR_IF_ENOUGH_SCRIPT,
                Collections.singletonList(invKey),
                String.valueOf(people)
        );

        if (ok == null || ok != 1L) {
            throw new IllegalStateException("NOT_ENOUGH_INVENTORY");
        }

        try {
            // 3) DB 예약 확정 insert
            UserReservation r = new UserReservation();
            r.setPopId(popupId);
            r.setPtsId(slotId);

            r.setUserId(userId);

            LocalDateTime urDateTime = date.atStartOfDay();
            r.setUrDateTime(urDateTime);

            r.setUrUserCnt(people);

            r.setUrStatus(true);

            userReservationMapper.insert(r);

            return r.getUrId();

        } catch (Exception e) {
            // 4) DB 실패 시 재고 롤백
            stringRedisTemplate.opsForValue().increment(invKey, people);
            throw e;
        }
    }

    /**
     * ✅ 결제 연동 확정 전용:
     * HOLD에서 이미 재고가 차감된 상태이므로 Redis 차감 없이 DB insert만 수행.
     */
    public Long createReservationConfirmedFromHold(Long popupId, Long slotId, LocalDate date, int people,Long userId) {
        if (popupId == null || slotId == null || date == null) {
            throw new IllegalArgumentException("popupId/slotId/date is required");
        }
        if (people <= 0) {
            throw new IllegalArgumentException("people must be > 0");
        }

        UserReservation r = new UserReservation();
        r.setPopId(popupId);
        r.setPtsId(slotId);

        r.setUserId(userId);

        LocalDateTime urDateTime = date.atStartOfDay();
        r.setUrDateTime(urDateTime);

        r.setUrUserCnt(people);
        r.setUrStatus(true);

        userReservationMapper.insert(r);
        return r.getUrId();
    }

    @Override
    public Map<String, Object> createReservationHold(Long popupId, Long slotId, LocalDate date, int people,Long userId) {
        if (popupId == null || slotId == null || date == null) {
            throw new IllegalArgumentException("popupId/slotId/date is required");
        }
        if (people <= 0) {
            throw new IllegalArgumentException("people must be > 0");
        }

        // 1) 재고키
        String invKey = buildInventoryKey(popupId, date, slotId);

        // 2) Lua로 원자적 차감
        Long ok = stringRedisTemplate.execute(
                DECR_IF_ENOUGH_SCRIPT,
                Collections.singletonList(invKey),
                String.valueOf(people)
        );

        if (ok == null || ok != 1L) {
            throw new IllegalStateException("NOT_ENOUGH_INVENTORY");
        }

        // 3) holdId / paymentId
        String holdId = UUID.randomUUID().toString().replace("-", "");
        String merchantUid = "hold-" + holdId;
        String paymentId = merchantUid;

        // 4) amount
        int amount = computeAmountFallback(popupId, people);

        // 5) hold 저장 (TTL)
        String holdKey = "hold:" + holdId;

        Map<String, String> holdMap = new HashMap<>();
        holdMap.put("popId", String.valueOf(popupId));
        holdMap.put("ptsId", String.valueOf(slotId));
        holdMap.put("date", date.toString());
        holdMap.put("people", String.valueOf(people));
        holdMap.put("invKey", invKey);

        holdMap.put("merchantUid", merchantUid);
        holdMap.put("paymentId", paymentId);
        holdMap.put("amount", String.valueOf(amount));

        stringRedisTemplate.opsForHash().putAll(holdKey, holdMap);
        stringRedisTemplate.expire(holdKey, HOLD_TTL);

        // 6) meta 저장(원복용)
        String metaKey = "holdmeta:" + holdId;
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("invKey", invKey);
        metaMap.put("people", String.valueOf(people));
        metaMap.put("paymentId", paymentId);

        stringRedisTemplate.opsForHash().putAll(metaKey, metaMap);
        stringRedisTemplate.expire(metaKey, HOLD_META_TTL);

        // 7) 만료 인덱스 등록
        long expireAtMillis = System.currentTimeMillis() + HOLD_TTL.toMillis();
        stringRedisTemplate.opsForZSet().add(HOLD_EXPIRY_ZSET, holdId, expireAtMillis);

        // 8) 결제 테이블 PENDING
        reservationPaymentMapper.insertPending(
                merchantUid,
                paymentId,
                popupId,
                slotId,
                userId,
                amount
        );

        // 9) 응답
        Map<String, Object> resp = new HashMap<>();
        resp.put("reservationId", safeParseLongHoldId(holdId)); // UUID면 null
        resp.put("holdId", holdId);

        resp.put("merchantUid", merchantUid);
        resp.put("paymentId", paymentId);
        resp.put("amount", amount);
        resp.put("ttlSeconds", HOLD_TTL.getSeconds());

        return resp;
    }

    private String buildInventoryKey(Long popupId, LocalDate date, Long slotId) {
        // ✅ 기존 포맷: yyyyMMdd
        String ymd = date.toString().replace("-", "");
        return "inv:" + popupId + ":" + ymd + ":" + slotId;
    }

    private int computeAmountFallback(Long popupId, int people) {
        int unitPrice = popupMapper.selectPriceByPopId(popupId);
        return unitPrice * Math.max(1, people);
    }

    private Long safeParseLongHoldId(String holdId) {
        try {
            return Long.parseLong(holdId);
        } catch (Exception e) {
            return null;
        }
    }
}
