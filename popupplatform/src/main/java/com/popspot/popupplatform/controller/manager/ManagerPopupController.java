package com.popspot.popupplatform.controller.manager;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.request.ManagerPopupUpdateRequest;
import com.popspot.popupplatform.dto.popup.response.ManagerPopupDetailResponse;
import com.popspot.popupplatform.dto.user.response.ManagerReservationResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.manager.ManagerPopupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/managers/popups")
@RequiredArgsConstructor
@Tag(name = "ManagerPopup", description = "매니저 팝업 관리 (상세/예약) API")
public class ManagerPopupController {

    private final ManagerPopupService managerPopupService;


    @Operation(summary = "매니저용 팝업 상세 조회", description = "자신이 등록한 팝업만 조회 가능합니다.")
    @GetMapping("/{popId}")
    public ResponseEntity<ManagerPopupDetailResponse> getPopupDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "팝업 ID") @PathVariable("popId") Long popId
    ) {
        return ResponseEntity.ok(managerPopupService.getPopupDetail(user.getUserId(), popId));
    }

    @Operation(summary = "팝업 예약자 목록 조회", description = "해당 팝업의 예약자 명단을 페이징하여 조회합니다.")
    @GetMapping("/{popId}/reservations")
    public ResponseEntity<PageDTO<ManagerReservationResponse>> getReservations(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "팝업 ID") @PathVariable("popId") Long popId,
            @ModelAttribute PageRequestDTO pageRequest
    ) {
        return ResponseEntity.ok(managerPopupService.getReservations(user.getUserId(), popId, pageRequest));
    }

    @Operation(summary = "팝업 기본 정보 수정", description = "제목, 설명, 가격 등 기본 정보를 수정합니다. (본인 팝업만 가능)")
    @PatchMapping("/{popId}")
    public ResponseEntity<Void> updatePopup(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "팝업 ID") @PathVariable("popId") Long popId,
            @RequestBody ManagerPopupUpdateRequest request
    ) {
        managerPopupService.updatePopupBasicInfo(user.getUserId(), popId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "팝업 삭제", description = "팝업을 삭제 처리합니다. (Soft Delete)")
    @DeleteMapping("/{popId}")
    public ResponseEntity<Void> deletePopup(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "팝업 ID") @PathVariable("popId") Long popId
    ) {
        managerPopupService.deletePopup(user.getUserId(), popId);
        return ResponseEntity.ok().build();
    }


}