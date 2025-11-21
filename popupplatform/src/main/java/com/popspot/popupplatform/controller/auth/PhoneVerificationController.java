package com.popspot.popupplatform.controller.auth;

import com.popspot.popupplatform.dto.user.request.PhoneVerificationRequest;
import com.popspot.popupplatform.service.auth.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 휴대폰 문자 인증용 컨트롤러
 * - /api/auth/phone/send   : 인증번호 전송
 * - /api/auth/phone/verify : 인증번호 검증
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/phone")
@Tag(name = "PhoneVerification", description = "휴대폰 문자 인증 API")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    /**
     * 인증번호 발송
     *
     * 사용 예:
     *  POST /api/auth/phone/send
     *  Body: { "phone": "01012345678" }
     */
    @Operation(summary = "인증 번호 문자 발송", description = "휴대폰 인증을 위한 문자 발송을 진행합니다.")
    @PostMapping("/send")
    public ResponseEntity<Void> sendVerification(@RequestBody PhoneVerificationRequest request) {

        phoneVerificationService.sendVerificationCode(request.getPhone());

        // 204 No Content
        return ResponseEntity.noContent().build();
    }

    /**
     * 인증번호 검증
     *
     * 사용 예:
     *  POST /api/auth/phone/verify
     *  Body: { "phone": "01012345678", "code": "123456" }
     *
     * 응답: true(성공) / false(실패)
     */
    @Operation(summary = "인증 번호 검증", description = "입력받은 인증번호가 유효한지 검사합니다")
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestBody PhoneVerificationRequest request) {

        boolean result = phoneVerificationService.verifyCode(
                request.getPhone(),
                request.getCode()
        );

        return ResponseEntity.ok(result);
    }
}
