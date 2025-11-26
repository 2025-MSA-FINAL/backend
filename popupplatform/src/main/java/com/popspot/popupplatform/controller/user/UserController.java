package com.popspot.popupplatform.controller.user;

import com.popspot.popupplatform.dto.user.response.CurrentUserResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User API", description = "회원 관련 API")
public class UserController {

    private final UserService userService;

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
}
