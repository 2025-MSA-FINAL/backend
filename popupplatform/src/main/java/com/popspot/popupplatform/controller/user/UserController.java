package com.popspot.popupplatform.controller.user;

import com.popspot.popupplatform.dto.global.UploadResultDto;
import com.popspot.popupplatform.dto.user.request.*;
import com.popspot.popupplatform.dto.user.response.CurrentUserResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.auth.PhoneVerificationService;
import com.popspot.popupplatform.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자(마이페이지) 관련 API")
public class UserController {

    private final UserService userService;
    private final PhoneVerificationService phoneVerificationService;

    /**
     * 현재 로그인한 사용자 정보 조회
     * - JWT 필터가 세팅한 CustomUserDetails에서 userId 꺼냄
     * - 로그인 안 되어 있으면 Security 레벨에서 401 발생
     */
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원의 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        CurrentUserResponse body = userService.getCurrentUser(userId);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "닉네임 변경", description = "현재 로그인한 사용자의 닉네임을 변경합니다.")
    @PutMapping("/me/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateNicknameRequest request
    ) {
        userService.updateNickname(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자기소개 변경", description = "현재 로그인한 사용자의 자기소개를 변경합니다.")
    @PatchMapping("/me/introduction")
    public ResponseEntity<Void> updateIntroduction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateIntroductionRequest request
    ) {
        userService.updateIntroduction(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 변경", description = "현재 로그인한 사용자의 이메일을 변경합니다.")
    @PutMapping("/me/email")
    public ResponseEntity<Void> updateEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateEmailRequest request
    ) {
        userService.updateEmail(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "휴대폰 번호 변경",
            description = "현재 로그인한 사용자의 휴대폰 번호를 변경합니다. (사전 문자 인증 필요)"
    )
    @PutMapping("/me/phone")
    public ResponseEntity<Void> updatePhone(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdatePhoneRequest request
    ) {
        // 🔹 1) 문자 인증번호 검증
        boolean verified = phoneVerificationService.verifyCode(
                request.getPhone(),
                request.getCode()
        );

        if (!verified) {
            // 인증 실패 시 400 Bad Request 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 🔹 2) 인증 성공하면 실제 휴대폰 번호 변경
        userService.updatePhone(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 검증 후 새 비밀번호로 변경합니다.")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "프로필 사진 수정", description = "회원의 프로필 사진을 수정합니다")
    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UploadResultDto dto
    ) {
        userService.updateProfile(user.getUserId(), dto); // dto 안에 profileImageUrl, profileImageKey 포함
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 확인", description = "비밀번호 변경용 비밀번호 확인")
    @PostMapping("/me/password/check")
    public ResponseEntity<Boolean> checkPassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CheckValidPwdDto dto
    ) {
        userService.checkVaildPwd(user.getUserId(), dto);
        return ResponseEntity.ok().body(true);
    }
}
