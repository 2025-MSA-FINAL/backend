// src/main/java/com/popspot/popupplatform/controller/auth/SignupCheckController.java
package com.popspot.popupplatform.controller.auth;

import com.popspot.popupplatform.service.auth.UserDuplicationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원가입 시 이메일 / 아이디 / 닉네임 중복 여부를 체크하는 컨트롤러
 * - 인증이 필요 없는 /api/auth 하위 경로로 두는 것을 가정
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/check")
public class SignupCheckController {

    private final UserDuplicationService userDuplicationService;

    /**
     * 이메일 중복 체크
     * 사용 예: GET /api/auth/check/email?email=test@example.com
     * 응답: true(중복) / false(사용 가능)
     */
    @Operation(summary = "이메일 중복 체크", description = "DB에 중복된 이메일이 있는지 검사")
    @GetMapping("/email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email) {
        boolean duplicate = userDuplicationService.isEmailDuplicate(email);
        return ResponseEntity.ok(duplicate);
    }

    /**
     * 로그인 아이디 중복 체크
     * 사용 예: GET /api/auth/check/login-id?loginId=testUser
     * 응답: true(중복) / false(사용 가능)
     */
    @Operation(summary = "아이디 중복 체크", description = "DB에 중복된 아이디가 있는지 검사")
    @GetMapping("/login-id")
    public ResponseEntity<Boolean> checkLoginId(@RequestParam("loginId") String loginId) {
        boolean duplicate = userDuplicationService.isLoginIdDuplicate(loginId);
        return ResponseEntity.ok(duplicate);
    }

    /**
     * 닉네임 중복 체크
     * 사용 예: GET /api/auth/check/nickname?nickname=닉네임
     * 응답: true(중복) / false(사용 가능)
     */
    @Operation(summary = "닉네임 중복 체크", description = "DB에 중복된 닉네임이 있는지 검사")
    @GetMapping("/nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nickname") String nickname) {
        boolean duplicate = userDuplicationService.isNicknameDuplicate(nickname);
        return ResponseEntity.ok(duplicate);
    }
}
