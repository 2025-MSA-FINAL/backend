package com.popspot.popupplatform.service.reservation;

import com.popspot.popupplatform.domain.reservation.UserReservation;
import com.popspot.popupplatform.dto.reservation.SlotWithReservationDto;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.ReservationErrorCode;
import com.popspot.popupplatform.mapper.reservation.PopupTimeSlotMapper;
import com.popspot.popupplatform.mapper.reservation.UserReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final PopupTimeSlotMapper popupTimeSlotMapper;
    private final UserReservationMapper userReservationMapper;

    // ✅ Redis remain 관리 (예약 설정에서 이미 사용 중인 템플릿)
    private final StringRedisTemplate stringRedisTemplate;

    // ✅ 테스트용 userId 자동 증가 로직 유지
    private final AtomicLong testUserId = new AtomicLong(1L);

    // TODO: 실제 로그인 사용자에서 받아오도록 교체
    private Long getCurrentUserId() {
        return testUserId.getAndIncrement();
    }

    /**
     * Redis remain key 포맷 (PopupReservationServiceImpl과 동일 컨벤션)
     * inv:{popId}:{yyyyMMdd}:{ptsId}
     */
    private String invKey(Long popId, LocalDate date, Long ptsId) {
        String ymd = date.toString().replace("-", "");
        return "inv:" + popId + ":" + ymd + ":" + ptsId;
    }

    /**
     * ✅ Lua로 "충분할 때만 차감" (원자성 보장)
     * - key가 없거나, remain < cnt 이면 실패(0)
     * - 성공이면 remain -= cnt 후 성공(1)
     */
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

    /**
     * 결제 없이: Redis(inventory remain) 차감 + USER_RESERVATION 즉시 확정
     * (추후 HOLD 붙일 걸 고려해서, DB 롤백 시 Redis 차감도 복구하도록 처리)
     */
    @Transactional
    public Long createReservationConfirmed(Long popupId,
                                           Long slotId,
                                           String dateStr,
                                           Integer people) {

        LocalDate date = LocalDate.parse(dateStr); // "yyyy-MM-dd"
        Long userId = getCurrentUserId();

        SlotWithReservationDto slot = popupTimeSlotMapper.findSlotWithPopupReservation(slotId);

        // slot null 체크
        if (slot == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND);
        }
        if (!slot.getPopId().equals(popupId)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_NOT_FOUND);
        }
        if (people == null || people <= 0 || slot.getMaxUserCnt() < people) {
            throw new CustomException(ReservationErrorCode.INVALID_PEOPLE_COUNT);
        }

        // ✅ 1) Redis remain 원자 차감
        String key = invKey(popupId, date, slotId);

        Long ok = stringRedisTemplate.execute(
                DECR_IF_ENOUGH_SCRIPT,
                List.of(key),
                String.valueOf(people)
        );

        if (ok == null || ok == 0L) {
            // 남은 좌석 부족(혹은 Redis 키가 없음)
            throw new CustomException(ReservationErrorCode.RESERVATION_SLOT_FULL);
        }

        // ✅ 2) DB 트랜잭션이 롤백되면 Redis 차감을 되돌린다 (안전장치)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    // 롤백이면 재고 원복
                    stringRedisTemplate.opsForValue().increment(key, people);
                }
            }
        });

        // ✅ 3) 예약 즉시 확정 insert
        LocalDateTime visitDateTime = LocalDateTime.of(date, slot.getStartTime());

        UserReservation reservation = UserReservation.builder()
                .popId(popupId)
                .ptsId(slotId)
                .userId(userId)
                .urDateTime(visitDateTime)
                .urUserCnt(people)
                .urStatus(true)
                .build();

        // useGeneratedKeys로 urId 채워짐 (UserReservationMapper.xml) :contentReference[oaicite:1]{index=1}
        userReservationMapper.insert(reservation);

        return reservation.getUrId();
    }
}
