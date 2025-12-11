package com.popspot.popupplatform.controller.admin;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.popspot.popupplatform.service.report.ReportService;
import com.popspot.popupplatform.dto.report.*;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

import lombok.RequiredArgsConstructor;

/**
 * 신고 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    /**
     * 대시보드용 신고 통계
     * GET /api/admin/reports/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = reportService.getReportStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 신고 목록 조회 (통합: 검색 + 상태 필터 + 카테고리 필터 + 정렬)
     * GET /api/admin/reports
     *
     * Query Parameters:
     * - keyword (optional): 검색어
     * - status (optional): pending | approved | rejected
     * - categoryId (optional): 신고 카테고리 ID
     * - page: 0-based 페이지 번호
     * - size: 페이지당 개수
     * - sortBy: createdAt | repStatus
     * - sortDir: ASC | DESC
     */
    @GetMapping
    public ResponseEntity<PageDTO<ReportListDTO>> getReportList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            PageRequestDTO pageRequest) {

        PageDTO<ReportListDTO> reports;

        // 검색어가 있으면 검색 (status, categoryId 필터도 함께 적용)
        // 검색어가 없으면 일반 목록 조회 (status, categoryId 필터만 적용)
        if (keyword != null && !keyword.trim().isEmpty()) {
            reports = reportService.searchReports(keyword, status, categoryId, pageRequest);
        } else {
            reports = reportService.getReportList(status, categoryId, pageRequest);
        }

        return ResponseEntity.ok(reports);
    }

    /**
     * 신고 상세 조회
     * GET /api/admin/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        ReportDetailDTO detail = reportService.getReportDetail(id);

        if (detail == null) {
            return ResponseEntity.status(404).body("Report not found");
        }

        return ResponseEntity.ok(detail);
    }

    /**
     * 신고 상태 변경
     * PUT /api/admin/reports/{id}?status=approved
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        // resolved 요청을 approved로 자동 변환 (하위 호환성)
        if ("resolved".equals(status)) {
            status = "approved";
        }

        // 유효성 검증
        if (!isValidStatus(status)) {
            return ResponseEntity.badRequest()
                    .body("Invalid status. Allowed: pending, approved, rejected");
        }

        boolean success = reportService.updateReportStatus(id, status);
        return success
                ? ResponseEntity.ok("updated")
                : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 유효한 상태 값인지 확인
     */
    private boolean isValidStatus(String status) {
        return "pending".equals(status)
                || "approved".equals(status)
                || "rejected".equals(status);
    }
}