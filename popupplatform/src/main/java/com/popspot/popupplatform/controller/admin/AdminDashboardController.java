package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.DashboardStatsDTO;
import com.popspot.popupplatform.dto.admin.PopularHashtagDTO;
import com.popspot.popupplatform.dto.admin.ViewDetailResponseDTO;
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
     *  전체 대시보드 통계 조회 (캐싱 적용)
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    /**
     *  인기 해시태그 필터링 조회 (성별 / 연령대)
     * GET /api/admin/dashboard/hashtags?gender=male&ageGroup=20대
     */
    @GetMapping("/hashtags")
    public ResponseEntity<List<PopularHashtagDTO>> getPopularHashtags(
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String gender
    ) {
        return ResponseEntity.ok(
                dashboardService.getPopularHashtagsFiltered(ageGroup, gender)
        );
    }

    /**
     *  조회수 히트맵의 셀을 클릭했을 때
     * 특정 날짜+시간대 상세 분석 정보 조회
     * GET /api/admin/dashboard/views/detail?date=2025-02-13&hour=14
     */
    @GetMapping("/views/detail")
    public ResponseEntity<ViewDetailResponseDTO> getViewDetail(
            @RequestParam String date,
            @RequestParam int hour
    ) {
        return ResponseEntity.ok(
                dashboardService.getViewDetail(date, hour)
        );
    }
}
