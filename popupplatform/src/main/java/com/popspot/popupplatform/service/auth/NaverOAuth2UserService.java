// src/main/java/com/popspot/popupplatform/global/service/auth/NaverOAuth2UserService.java
package com.popspot.popupplatform.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class NaverOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final RestClient rest = RestClient.create();
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        // 1) provider 체크 ("naver" 아니면 에러)
        if (!"naver".equals(req.getClientRegistration().getRegistrationId())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_request"),
                    "Unsupported provider"
            );
        }

        // 2) user-info-uri 가져오기 (application.yml에 설정한 값)
        String userInfoUri = req.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();
        String accessToken = req.getAccessToken().getTokenValue();

        String json;
        // 3) 네이버 유저 정보 API 호출
        try {
            json = rest.get()
                    .uri(userInfoUri)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token"),
                    "Failed to fetch user info",
                    e
            );
        }

        try {
            // ===== JSON 파싱: id만 사용 =====
            JsonNode root = om.readTree(json);
            JsonNode response = root.path("response");

            // 네이버에서 내려주는 고유 id
            String id = response.path("id").asText(null);

            if (id == null || id.isBlank()) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_user_info"),
                        "Missing id"
                );
            }

            // 우리가 다음 단계에서 쓸 최소 정보만 넣어둠
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("provider", "NAVER");   // DB에서는 대문자/소문자로 통일해서 사용
            attrs.put("providerId", id);      // USER_SOCIAL.oauth_id 에 들어갈 값

            // ===== 여기까지가 변경된 부분, 아래는 그대로 =====

            // ROLE_GUEST 권한으로 기본 로그인
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST")),
                    attrs,
                    "providerId" // nameAttributeKey
            );
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "Failed to parse user info",
                    e
            );
        }
    }
}
