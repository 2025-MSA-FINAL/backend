package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.service.admin.AdminPopupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/popups")
@RequiredArgsConstructor
public class AdminPopupController {

    private final AdminPopupService popupService;


    /**
     * 🆕 팝업스토어 전체 통계 조회 (필터 무관)
     * GET /api/admin/popups/stats
     *
     * 응답 예시:
     * {
     *   "total": 89,
     *   "pending": 12,
     *   "active": 45,
     *   "ended": 32
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPopupStats() {
        Map<String, Object> stats = popupService.getPopupStats();
        return ResponseEntity.ok(stats);
    }




    /**
     * 팝업스토어 목록 조회 (통합 검색/필터/페이징)
     * GET /api/admin/popups?page=0&size=10&keyword=BTS&status=active&moderation=pending
     *
     * 모든 파라미터는 optional이며, 조합하여 사용 가능
     *
     * @param pageRequest 페이징 정보 (page, size, sortBy, sortDir)
     * @param keyword 검색어 (팝업명, 위치 등)
     * @param status 팝업 상태 (upcoming, active, ended)
     * @param moderation 승인 상태 (pending, approved, rejected)
     * @return 페이징된 팝업스토어 목록
     *
     * 사용 예시:
     * - 전체 조회: /api/admin/popups?page=0&size=10
     * - 검색: /api/admin/popups?keyword=BTS&page=0
     * - 필터: /api/admin/popups?status=active&moderation=pending&page=0
     * - 조합: /api/admin/popups?keyword=팝업&status=active&page=0
     */
    @GetMapping
    public ResponseEntity<PageDTO<PopupStoreListDTO>> getPopupList(
            PageRequestDTO pageRequest,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String moderation) {

        PageDTO<PopupStoreListDTO> popups = popupService.getPopupList(
                pageRequest, keyword, status, moderation
        );
        return ResponseEntity.ok(popups);
    }

    /**
     * 팝업스토어 상세 조회
     * GET /api/admin/popups/{popId}
     */
    @GetMapping("/{popId}")
    public ResponseEntity<PopupStoreListDTO> getPopupDetail(@PathVariable Long popId) {
        PopupStoreListDTO popup = popupService.getPopupDetail(popId);
        if (popup == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(popup);
    }

    /**
     * 팝업스토어 승인
     * PUT /api/admin/popups/{popId}/approve
     */
    @PutMapping("/{popId}/approve")
    public ResponseEntity<String> approvePopup(@PathVariable Long popId) {
        boolean success = popupService.updateModerationStatus(popId, true);
        return success ? ResponseEntity.ok("approved") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 반려
     * PUT /api/admin/popups/{popId}/reject
     */
    @PutMapping("/{popId}/reject")
    public ResponseEntity<String> rejectPopup(@PathVariable Long popId) {
        boolean success = popupService.updateModerationStatus(popId, false);
        return success ? ResponseEntity.ok("rejected") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 상태 변경
     * PUT /api/admin/popups/{popId}/status?status=active
     */
    @PutMapping("/{popId}/status")
    public ResponseEntity<String> updatePopupStatus(
            @PathVariable Long popId,
            @RequestParam String status) {
        boolean success = popupService.updatePopupStatus(popId, status);
        return success ? ResponseEntity.ok("updated") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 팝업스토어 삭제 (soft delete)
     * DELETE /api/admin/popups/{popId}
     */
    @DeleteMapping("/{popId}")
    public ResponseEntity<String> deletePopup(@PathVariable Long popId) {
        boolean success = popupService.deletePopup(popId);
        return success ? ResponseEntity.ok("deleted") : ResponseEntity.badRequest().body("fail");
    }
}