package com.popspot.popupplatform.controller.manager;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.response.PopupListItemResponse;
import com.popspot.popupplatform.dto.user.request.ChangePasswordRequest;
import com.popspot.popupplatform.dto.user.request.UpdateEmailRequest;
import com.popspot.popupplatform.dto.user.request.UpdatePhoneRequest;
import com.popspot.popupplatform.dto.user.response.ManagerProfileResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.manager.ManagerPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/managers/me")
@RequiredArgsConstructor
@Tag(name = "ManagerPage", description = "매니저 마이페이지 관련 API")
public class ManagerPageController {

    private final ManagerPageService managerPageService;

    @Operation(summary = "매니저 프로필 조회", description = "브랜드명(닉네임)은 수정 불가하며 조회만 가능합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ManagerProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(managerPageService.getMyProfile(user.getUserId()));
    }

    @Operation(summary = "매니저 이메일 수정", description = "중복된 이메일일 경우 409 Conflict 에러를 반환합니다.")
    @PatchMapping("/email")
    public ResponseEntity<Void> updateEmail(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UpdateEmailRequest request
    ) {
        managerPageService.updateEmail(user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매니저 전화번호 수정", description = "중복된 전화번호일 경우 409 Conflict 에러를 반환합니다.")
    @PatchMapping("/phone")
    public ResponseEntity<Void> updatePhone(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UpdatePhoneRequest request
    ) {
        managerPageService.updatePhone(user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매니저 비밀번호 변경", description = "현재 비밀번호가 틀리면 400 Bad Request 에러를 반환합니다.")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody ChangePasswordRequest request
    ) {
        managerPageService.changePassword(user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내가 등록한 팝업 목록 조회",
            description = "status: 진행 상태, moderation: 승인 상태 필터"
    )
    @GetMapping("/popups")
    public ResponseEntity<PageDTO<PopupListItemResponse>> getMyPopups(
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute PageRequestDTO pageRequest,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "ALL", name = "moderation") String moderation
    ) {
        return ResponseEntity.ok(
                managerPageService.getMyPopups(user.getUserId(), pageRequest, status, moderation)
        );
    }

}