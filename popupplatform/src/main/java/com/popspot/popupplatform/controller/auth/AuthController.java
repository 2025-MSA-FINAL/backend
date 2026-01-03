package com.popspot.popupplatform.controller.auth;

import com.popspot.popupplatform.dto.user.LoginUserDto;
import com.popspot.popupplatform.dto.user.enums.UserStatus;
import com.popspot.popupplatform.dto.user.request.LoginRequest;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.auth.AuthCookieService;
import com.popspot.popupplatform.global.utils.JwtTokenProvider;
import com.popspot.popupplatform.mapper.user.UserMapper;
import com.popspot.popupplatform.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "로그인/로그아웃 인증 API")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthCookieService authCookieService;
    private final UserService userService;

    /**
     * 일반 로그인
     * - USER_GENERAL 기준
     * - 일반 회원가입 기능은 없음 (DB에 미리 계정이 존재해야 함)
     */
    @Operation(summary = "일반 로그인", description = "아이디와 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        // 1) 계정 조회
        LoginUserDto user = userMapper.findGeneralUserByLoginId(request.getLoginId())
                .orElse(null);

        if (user == null) {
            throw new CustomException(AuthErrorCode.LOGIN_FAILED);
        }

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.LOGIN_FAILED);
        }

        // 3) 상태 체크 (ACTIVE만 허용)  여기만 enum 사용으로 변경
        if (!UserStatus.ACTIVE.name().equalsIgnoreCase(user.getStatus())) {
            throw new CustomException(AuthErrorCode.INACTIVE_USER);
        }

        // 4) 토큰 생성
        Map<String, Object> claims = Map.of(
                "userId", user.getUserId(),
                "role", user.getRole()
        );
        String subject = String.valueOf(user.getUserId());

        String accessToken  = jwtTokenProvider.createAccessToken(subject, claims);
        String refreshToken = jwtTokenProvider.createRefreshToken(subject, claims);

        // 5) 공통 서비스로 쿠키 설정
        authCookieService.addLoginCookies(response, accessToken, refreshToken);

        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        // AccessToken + RefreshToken 삭제
        authCookieService.clearAuthCookies(response);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        authCookieService.clearAuthCookies(response);
        userService.deleteUser(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "토큰 재발급", description = "RefreshToken 쿠키로 AccessToken을 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // refreshToken 쿠키가 없거나 유효하지 않으면 401
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        // refreshToken에서 subject/claims 추출
        String subject = jwtTokenProvider.getSubject(refreshToken); // 보통 userId 문자열
        String role = String.valueOf(jwtTokenProvider.getClaims(refreshToken).get("role"));

        Map<String, Object> claims = Map.of(
                "userId", Long.valueOf(subject),
                "role", role
        );

        // 새 accessToken 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(subject, claims);

        // 쿠키 재세팅 (access 갱신 + refresh는 같은 값으로 다시 세팅)
        authCookieService.addLoginCookies(response, newAccessToken, refreshToken);

        return ResponseEntity.noContent().build();
    }

}
