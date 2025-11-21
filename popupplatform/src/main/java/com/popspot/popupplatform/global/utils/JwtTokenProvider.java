// src/main/java/com/popspot/popupplatform/global/utils/JwtTokenProvider.java
package com.popspot.popupplatform.global.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessValidityMs;
    private final long refreshValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds}") long accessValiditySec,
            @Value("${jwt.refresh-token-validity-seconds}") long refreshValiditySec
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValiditySec * 1000;
        this.refreshValidityMs = refreshValiditySec * 1000;
    }

    /* ===================== Access / Refresh ===================== */

    public String createAccessToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return parse(token).getBody();
    }

    /* ===================== 소셜 회원가입용 signupToken ===================== */

    /**
     * 소셜 회원가입 전용 토큰
     * - purpose = "signup"
     * - provider / providerId / 이메일/이름 등 최소 정보 포함
     * - 만료 시간은 10분 고정 (프론트에서 짧게만 사용)
     */
    public String createSignupToken(String provider,
                                    String providerId,
                                    Map<String, Object> profileClaims) {
        long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .setSubject("social-signup")
                .claim("purpose", "signup")
                .claim("provider", provider)
                .claim("providerId", providerId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + Duration.ofMinutes(10).toMillis()));

        if (profileClaims != null) {
            builder.addClaims(profileClaims);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseSignupToken(String token) {
        Claims c = parse(token).getBody();
        if (!"signup".equals(c.get("purpose"))) {
            throw new JwtException("Invalid purpose");
        }
        return c;
    }

    /* ===================== 공통 파서 ===================== */

    private Jws<Claims> parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            throw e;
        }
    }
}
