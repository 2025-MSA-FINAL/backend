// src/main/java/com/popspot/popupplatform/controller/auth/SocialAuthController.java
package com.popspot.popupplatform.controller.auth;

import com.popspot.popupplatform.dto.user.request.SocialSignupRequest;
import com.popspot.popupplatform.dto.user.UserDto;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;

import com.popspot.popupplatform.global.utils.JwtTokenProvider;
import com.popspot.popupplatform.service.auth.AuthCookieService;
import com.popspot.popupplatform.service.auth.RefreshTokenRedisService;
import com.popspot.popupplatform.service.auth.SocialAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieService authCookieService;
    private final RefreshTokenRedisService refreshTokenRedisService;

    @Operation(summary = "회원가입", description = "소셜 인증후 진행되는 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @CookieValue(value = "signupToken", required = false) String signupToken,
            @RequestBody SocialSignupRequest request,
            HttpServletResponse response
    ) {
        if (signupToken == null) {
            throw new CustomException(AuthErrorCode.INVALID_TOKEN);
        }

        Claims claims;
        try {
            claims = jwtTokenProvider.parseSignupToken(signupToken);
        } catch (ExpiredJwtException e) {
            // 토큰 만료
            throw new CustomException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            // 서명/형식 등 토큰 문제
            throw new CustomException(AuthErrorCode.INVALID_TOKEN);
        }

        UserDto user = socialAuthService.signupFromSocial(claims, request);

        Map<String, Object> tokenClaims = Map.of(
                "userId", user.getUserId(),
                "role", user.getRole()
        );
        String subject = String.valueOf(user.getUserId());

        String accessToken  = jwtTokenProvider.createAccessToken(subject, tokenClaims);
        String refreshToken = jwtTokenProvider.createRefreshToken(subject, tokenClaims);

        refreshTokenRedisService.save(
                user.getUserId(),
                refreshToken,
                jwtTokenProvider.getRefreshTtl()
        );

        // ✅ 쿠키 세팅 + signupToken 제거
        authCookieService.addLoginCookies(response, accessToken, refreshToken);
        authCookieService.clearSignupCookie(response);

        return ResponseEntity.noContent().build(); // 204
    }
}
