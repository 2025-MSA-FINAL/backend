package com.popspot.popupplatform.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final StringRedisTemplate redis;

    private String key(Long userId) {
        return "auth:refresh:" + userId;
    }

    /** refreshToken 저장 */
    public void save(Long userId, String refreshToken, Duration ttl) {
        redis.opsForValue().set(key(userId), refreshToken, ttl);
    }

    /** refreshToken 조회 */
    public String get(Long userId) {
        return redis.opsForValue().get(key(userId));
    }

    /** refreshToken 삭제 */
    public void delete(Long userId) {
        redis.delete(key(userId));
    }
}