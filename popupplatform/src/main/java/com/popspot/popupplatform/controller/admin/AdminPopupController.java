package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.service.admin.AdminPopupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/popups")
@RequiredArgsConstructor
public class AdminPopupController {

    private final AdminPopupService popupService;

    /**
     * 팝업스토어 목록 조회 (페이지네이션)
     * GET /api/admin/popups?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping
    public ResponseEntity<PageDTO<PopupStoreListDTO>> getPopupList(PageRequestDTO pageRequest) {
        PageDTO<PopupStoreListDTO> popups = popupService.getPopupList(pageRequest);
        return ResponseEntity.ok(popups);
    }

    /**
     * 승인 대기 중인 팝업스토어 목록
     * GET /api/admin/popups/pending?page=0&size=10
     */
    @GetMapping("/pending")
    public ResponseEntity<PageDTO<PopupStoreListDTO>> getPendingPopupList(PageRequestDTO pageRequest) {
        PageDTO<PopupStoreListDTO> popups = popupService.getPendingPopupList(pageRequest);
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

    /**
     * 팝업스토어 검색
     * GET /api/admin/popups/search?keyword=팝업&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<PageDTO<PopupStoreListDTO>> searchPopups(
            @RequestParam String keyword,
            PageRequestDTO pageRequest) {
        PageDTO<PopupStoreListDTO> popups = popupService.searchPopups(keyword, pageRequest);
        return ResponseEntity.ok(popups);
    }
}