package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.ModerationUpdateRequestDTO;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.service.admin.AdminPopupService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/popups")
@RequiredArgsConstructor
public class AdminPopupController {

    private final AdminPopupService popupService;

    /**
     * 팝업스토어 전체 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPopupStats() {
        return ResponseEntity.ok(popupService.getPopupStats());
    }

    /**
     * 팝업스토어 목록 조회
     */
    @GetMapping
    public ResponseEntity<PageDTO<PopupStoreListDTO>> getPopupList(
            PageRequestDTO pageRequest,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String moderation,
            @RequestParam(required = false, defaultValue = "active") String deletedFilter) {

        return ResponseEntity.ok(popupService.getPopupList(pageRequest, keyword, status, moderation, deletedFilter));
    }

    /**
     * 팝업스토어 상세 조회
     */
    @GetMapping("/{popId}")
    public ResponseEntity<PopupStoreListDTO> getPopupDetail(@PathVariable Long popId) {
        PopupStoreListDTO popup = popupService.getPopupDetail(popId);
        return popup == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(popup);
    }

    /**
     * 승인 상태 변경 (통합 API)
     * @RequestBody에 ModerationUpdateRequestDTO 적용
     */
    @PutMapping("/{popId}/moderation")
    public ResponseEntity<String> updateModerationStatus(
            @PathVariable Long popId,
            @RequestBody ModerationUpdateRequestDTO request) {

        // Service 호출 시 3개 인자 전달 (id, status, reason)
        boolean success = popupService.updateModerationStatus(
                popId,
                request.getStatus(),
                request.getReason()
        );

        if (!success) {
            return ResponseEntity.badRequest().body("fail");
        }

        String statusText = request.getStatus() == null ? "대기" :
                request.getStatus() ? "승인" : "거절";

        return ResponseEntity.ok(statusText + " 상태로 변경되었습니다.");
    }

    /**
     * 팝업스토어 승인 (편의 API)
     */
    @PutMapping("/{popId}/approve")
    public ResponseEntity<String> approvePopup(@PathVariable Long popId) {
        // 승인이므로 사유(reason)는 null로 전달
        boolean success = popupService.updateModerationStatus(popId, true, null);
        return success ? ResponseEntity.ok("approved") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 반려 (편의 API)
     */
    @PutMapping("/{popId}/reject")
    public ResponseEntity<String> rejectPopup(
            @PathVariable Long popId,
            @RequestBody(required = false) RejectRequest request) {

        String reason = (request != null) ? request.getReason() : null;

        // 반려이므로 사유(reason) 전달
        boolean success = popupService.updateModerationStatus(popId, false, reason);
        return success ? ResponseEntity.ok("rejected") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 운영 상태 변경
     */
    @PutMapping("/{popId}/status")
    public ResponseEntity<String> updatePopupStatus(
            @PathVariable Long popId,
            @RequestParam String status) {
        boolean success = popupService.updatePopupStatus(popId, status);
        return success ? ResponseEntity.ok("updated") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 삭제 (Soft Delete)
     */
    @DeleteMapping("/{popId}")
    public ResponseEntity<String> deletePopup(
            @PathVariable Long popId,
            @RequestParam(required = false) String reason) {

        boolean success = popupService.deletePopup(popId, reason);
        return success ? ResponseEntity.ok("deleted") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 복구
     */
    @PutMapping("/{popId}/restore")
    public ResponseEntity<String> restorePopup(@PathVariable Long popId) {
        boolean success = popupService.restorePopup(popId);
        return success ? ResponseEntity.ok("restored") : ResponseEntity.badRequest().body("fail");
    }
}

/**
 * 거절 요청 DTO (편의 API용)
 */
@Getter
@Setter
class RejectRequest {
    private String reason;
}