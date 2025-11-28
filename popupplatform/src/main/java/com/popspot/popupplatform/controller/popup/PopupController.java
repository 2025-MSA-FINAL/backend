package com.popspot.popupplatform.controller.popup;

import com.popspot.popupplatform.dto.popup.request.PopupCreateRequest;
import com.popspot.popupplatform.service.popup.PopupService;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/popups")
@RequiredArgsConstructor
@Tag(name = "Popup", description = "팝업 스토어 관리 API")
public class PopupController {

    private final PopupService popupService;

    /**
     * 팝업 스토어 등록
     * - [POST] /api/popups
     * - Content-Type: application/json
     * - Body: PopupCreateRequest (이미지 URL 포함)
     */
    @Operation(summary = "팝업 스토어 등록", description = "이미지 업로드 API에서 받은 URL을 포함하여 팝업을 등록합니다. (매니저 권한 필요)")
    @ApiResponse(responseCode = "201", description = "팝업 등록 성공")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createPopup(


            @RequestBody @Valid PopupCreateRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 로그인 체크 (토큰 없이 접근 시 401 에러)
        if (userDetails == null) {
            throw new CustomException(AuthErrorCode.NO_AUTH_TOKEN);
        }

        // 2. ID 추출
        Long managerId = Long.parseLong(userDetails.getUsername());

        log.info("팝업 등록 요청: managerId={}, title={}", managerId, request.getPopName());

        // 3. 서비스 호출 (파일 파라미터 없이 DTO와 매니저ID만 전달)
        Long popupId = popupService.registerPopup(request, managerId);

        // 4. 201 Created 응답 + Location 헤더
        return ResponseEntity.created(URI.create("/api/popups/" + popupId)).build();
    }
}