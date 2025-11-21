// src/main/java/com/popspot/popupplatform/global/security/OAuth2SuccessHandler.java
package com.popspot.popupplatform.global.security;

import com.popspot.popupplatform.dto.user.UserDto;
import com.popspot.popupplatform.service.auth.AuthCookieService;
import com.popspot.popupplatform.global.utils.JwtTokenProvider;
import com.popspot.popupplatform.mapper.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwt;
    private final UserMapper userMapper;
    private final AuthCookieService authCookieService;

    @Value("${app.frontend-url}")
    private String frontend;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String provider   = (String) oAuth2User.getAttribute("provider");
        String providerId = (String) oAuth2User.getAttribute("providerId");

        if (provider == null || providerId == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid OAuth2 user info");
            return;
        }

        Optional<UserDto> userOpt =
                userMapper.findByProviderAndProviderId(provider, providerId);

        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();

            Map<String, Object> claims = Map.of(
                    "userId", user.getUserId(),
                    "role", user.getRole()
            );
            String subject = String.valueOf(user.getUserId());

            String accessToken  = jwt.createAccessToken(subject, claims);
            String refreshToken = jwt.createRefreshToken(subject, claims);

            // ✅ 공통 서비스로 쿠키 설정
            authCookieService.addLoginCookies(res, accessToken, refreshToken);

            String redirect = UriComponentsBuilder.fromHttpUrl(frontend)
                    .path("/login-success")
                    .build(true).toUriString();
            res.sendRedirect(redirect);
        } else {
            // 신규 유저
            String signupToken = jwt.createSignupToken(provider, providerId, new HashMap<>());

            // ✅ 공통 서비스로 signupToken 쿠키 설정
            authCookieService.addSignupCookie(res, signupToken);

            String redirect = UriComponentsBuilder.fromHttpUrl(frontend)
                    .path("/signup/social")
                    .build(true).toUriString();
            res.sendRedirect(redirect);
        }
    }
}
