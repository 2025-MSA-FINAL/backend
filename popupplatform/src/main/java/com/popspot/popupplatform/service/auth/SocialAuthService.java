// src/main/java/com/popspot/popupplatform/service/auth/SocialAuthService.java
package com.popspot.popupplatform.service.auth;

import com.popspot.popupplatform.dto.user.UserDto;
import com.popspot.popupplatform.dto.user.enums.UserRole;
import com.popspot.popupplatform.dto.user.enums.UserStatus;
import com.popspot.popupplatform.dto.user.request.SocialSignupRequest;
import com.popspot.popupplatform.mapper.user.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 소셜 회원가입 처리
 * - 소셜 로그인(OAuth2)에서 신규 유저로 판정된 경우만 호출
 * - USER + USER_SOCIAL 두 테이블에 insert
 */
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${aws.s3.default-profile.url}")
    private String defaultProfileUrl;

    /**
     * 소셜 회원가입 처리
     *
     * @param claims OAuth2SuccessHandler에서 발급한 signupToken의 클레임
     *               - provider, providerId 포함
     * @param req    프론트에서 추가로 입력받은 정보(닉네임, 전화번호, 성별, 생년, 프로필 업로드 결과 등)
     * @return 생성된 UserDto (userId 포함)
     */
    public UserDto signupFromSocial(Claims claims, SocialSignupRequest req) {
        String provider   = claims.get("provider", String.class);
        String providerId = claims.get("providerId", String.class);

        UserDto dto = new UserDto();
        dto.setProvider(provider);
        dto.setProviderId(providerId);

        // 기본 정보
        dto.setEmail(req.getEmail());
        dto.setName(req.getName());

        dto.setNickname(req.getNickname());
        dto.setGender(req.getGender());
        dto.setPhone(req.getPhone());
        dto.setBirthYear(req.getBirthYear());

        dto.setLoginId(req.getLoginId());
        dto.setLoginPwd(passwordEncoder.encode(req.getPassword()));

        // 프로필 이미지 URL (S3 업로드 결과 or 기본 이미지)
        if (StringUtils.hasText(req.getProfileImageUrl())) {
            dto.setProfileImage(req.getProfileImageUrl());
        } else {
            dto.setProfileImage(defaultProfileUrl);
        }

        dto.setStatus(UserStatus.ACTIVE.name());
        dto.setRole(UserRole.USER.name());

        // 1) USER insert
        userMapper.insertUser(dto);   // dto.userId 세팅됨

        // 2) USER_SOCIAL insert
        userMapper.insertUserSocial(dto);

        // 3) USER_GENERAL insert
        userMapper.insertUserGeneral(dto);

        return dto;
    }
}
