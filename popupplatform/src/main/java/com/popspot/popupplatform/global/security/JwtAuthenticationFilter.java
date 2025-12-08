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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // WebSocket handshake 경로 완전 제외
        return uri.startsWith("/ws-stomp") ||
                uri.startsWith("/pub") ||
                uri.startsWith("/sub");
    }

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

                if (userIdObj != null) {
                    Long userId = Long.valueOf(String.valueOf(userIdObj));

                    // DB에서 계정 정보 조회 (UserDetailsService 사용)
                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(String.valueOf(userId));

                    // 계정 상태가 비활성인 경우 (예: DELETED / PENDING 등)
                    if (!userDetails.isEnabled()) {
                        SecurityContextHolder.clearContext();
                        request.setAttribute("authErrorCode", AuthErrorCode.INACTIVE_USER);
                    } else {
                        Authentication authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute("authErrorCode", AuthErrorCode.EXPIRED_TOKEN);
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute("authErrorCode", AuthErrorCode.INVALID_TOKEN);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        } else {
            // 토큰 자체 없음
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
