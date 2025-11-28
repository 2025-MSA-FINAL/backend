package com.popspot.popupplatform.controller.admin;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.popspot.popupplatform.service.report.ReportService;
import com.popspot.popupplatform.dto.report.*;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

import lombok.RequiredArgsConstructor;

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
     * 신고 목록 조회 (페이지네이션)
     * GET /api/admin/reports?page=0&size=10&sortBy=createdAt&sortDir=desc&status=pending
     */
    @GetMapping
    public ResponseEntity<PageDTO<ReportListDTO>> getReportList(
            @RequestParam(required = false) String status,
            PageRequestDTO pageRequest) {
        PageDTO<ReportListDTO> reports = reportService.getReportList(status, pageRequest);
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
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        boolean ok = reportService.updateReportStatus(id, status);
        return ok ? ResponseEntity.ok("updated") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 신고 검색
     * GET /api/admin/reports/search?keyword=검색어&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<PageDTO<ReportListDTO>> searchReports(
            @RequestParam String keyword,
            PageRequestDTO pageRequest) {
        PageDTO<ReportListDTO> reports = reportService.searchReports(keyword, pageRequest);
        return ResponseEntity.ok(reports);
    }
}
