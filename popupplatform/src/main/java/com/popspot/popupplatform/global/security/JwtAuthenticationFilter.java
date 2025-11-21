// src/main/java/com/popspot/popupplatform/global/security/JwtAuthenticationFilter.java
package com.popspot.popupplatform.global.security;

import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.utils.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.parseAccessToken(token);

                Object userIdObj = claims.get("userId");
                String role = (String) claims.get("role");

                if (userIdObj != null && StringUtils.hasText(role)) {
                    String principal = String.valueOf(userIdObj);
                    List<GrantedAuthority> authorities =
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                // 🔴 토큰 만료: 인증만 지우고, 에러코드 심어둠
                SecurityContextHolder.clearContext();
                request.setAttribute("authErrorCode", AuthErrorCode.EXPIRED_TOKEN);
            } catch (JwtException e) {
                // 🔴 유효하지 않은 토큰(서명, 형식 등)
                SecurityContextHolder.clearContext();
                request.setAttribute("authErrorCode", AuthErrorCode.INVALID_TOKEN);
            } catch (Exception e) {
                // 기타 예외는 일단 인증만 제거 (에러코드 심지 않음)
                SecurityContextHolder.clearContext();
            }
        } else {
            // 토큰 자체가 없음 → 나중에 EntryPoint에서 NO_AUTH_TOKEN으로 쓸 수 있게 심어둘 수도 있음
            request.setAttribute("authErrorCode", AuthErrorCode.NO_AUTH_TOKEN);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 헤더 또는 쿠키에서 토큰 추출
     * - Authorization: Bearer xxx
     * - 또는 accessToken 쿠키
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 쿠키에서도 시도
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName()) &&
                        StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
