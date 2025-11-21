// src/main/java/com/popspot/popupplatform/global/service/auth/AuthCookieService.java
package com.popspot.popupplatform.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.cookie.access-max-age-seconds:3600}")
    private long accessCookieMaxAgeSec;

    @Value("${app.cookie.refresh-max-age-seconds:1209600}")
    private long refreshCookieMaxAgeSec;

    @Value("${app.cookie.signup-max-age-minutes:10}")
    private long signupCookieMaxAgeMin;

    /* ========= 공통 빌더 ========= */

    private ResponseCookie.ResponseCookieBuilder base(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/");
    }

    /* ========= 로그인 완료 시(access/refresh) ========= */

    public void addLoginCookies(HttpServletResponse res,
                                String accessToken,
                                String refreshToken) {

        ResponseCookie accessCookie = base("accessToken", accessToken)
                .maxAge(Duration.ofSeconds(accessCookieMaxAgeSec))
                .build();

        ResponseCookie refreshCookie = base("refreshToken", refreshToken)
                .maxAge(Duration.ofSeconds(refreshCookieMaxAgeSec))
                .build();

        res.addHeader("Set-Cookie", accessCookie.toString());
        res.addHeader("Set-Cookie", refreshCookie.toString());
    }

    /* ========= 로그아웃 시 auth 쿠키 제거 ========= */

    public void clearAuthCookies(HttpServletResponse response) {
        // ✅ 로그인 때와 동일한 옵션(secure, sameSite, path)을 사용해서 이름과 maxAge만 0으로 변경
        ResponseCookie access = base("accessToken", "")
                .maxAge(0)
                .build();

        ResponseCookie refresh = base("refreshToken", "")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", access.toString());
        response.addHeader("Set-Cookie", refresh.toString());
    }

    /* ========= 소셜 회원가입용 signupToken ========= */

    public void addSignupCookie(HttpServletResponse res, String signupToken) {
        ResponseCookie signupCookie = base("signupToken", signupToken)
                .maxAge(Duration.ofMinutes(signupCookieMaxAgeMin))
                .build();

        res.addHeader("Set-Cookie", signupCookie.toString());
    }

    public void clearSignupCookie(HttpServletResponse res) {
        ResponseCookie clearSignup = base("signupToken", "")
                .maxAge(0)
                .build();

        res.addHeader("Set-Cookie", clearSignup.toString());
    }
}