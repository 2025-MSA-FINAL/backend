package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.DashboardStatsDTO;
import com.popspot.popupplatform.dto.admin.PopularHashtagDTO;
import com.popspot.popupplatform.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * 대시보드 통계 조회
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 인기 해시태그 필터링 조회 (연령/성별)
     * GET /api/admin/dashboard/hashtags?ageGroup=20대&gender=male
     */
    @GetMapping("/hashtags")
    public ResponseEntity<List<PopularHashtagDTO>> getPopularHashtags(
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String gender) {
        List<PopularHashtagDTO> hashtags = dashboardService.getPopularHashtagsFiltered(ageGroup, gender);
        return ResponseEntity.ok(hashtags);
    }
}
