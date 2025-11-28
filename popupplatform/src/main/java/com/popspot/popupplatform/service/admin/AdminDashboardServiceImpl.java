package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.DashboardStatsDTO;
import com.popspot.popupplatform.dto.admin.PopularHashtagDTO;
import com.popspot.popupplatform.mapper.admin.AdminDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper dashboardMapper;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 기본 유저 통계
        stats.setTotalUsers(dashboardMapper.countTotalUsers());
        stats.setNewUsersToday(dashboardMapper.countNewUsersToday());
        stats.setNewUsersThisWeek(dashboardMapper.countNewUsersThisWeek());
        stats.setNewUsersThisMonth(dashboardMapper.countNewUsersThisMonth());

        // 팝업스토어 통계
        stats.setTotalPopupStores(dashboardMapper.countTotalPopups());
        stats.setActivePopupStores(dashboardMapper.countActivePopups());
        stats.setPendingApproval(dashboardMapper.countPendingApprovalPopups());
        stats.setEndingSoon(dashboardMapper.countEndingSoonPopups());

        // 신고 통계
        stats.setTotalReports(dashboardMapper.countTotalReports());
        stats.setPendingReports(dashboardMapper.countReportsByStatus("pending"));
        stats.setApprovedReports(dashboardMapper.countReportsByStatus("approved"));
        stats.setResolvedReports(dashboardMapper.countReportsByStatus("resolved"));
        stats.setRejectedReports(dashboardMapper.countReportsByStatus("rejected"));

        // ===== 필수 통계 =====

        // 1. 성별/연령별 유저 분포
        stats.setUserDemographics(dashboardMapper.getUserDemographics());

        // 2. 이번 주 인기 팝업 Top 10 (조회수 기준)
        stats.setTopPopupsThisWeek(dashboardMapper.getTopPopupsThisWeek());

        // 3. 인기 해시태그 (찜 기준) - 전체
        stats.setPopularHashtags(dashboardMapper.getPopularHashtags(null, null));

        // 4. 카테고리별 신고 건수
        stats.setReportCategoryStats(dashboardMapper.getReportCategoryStats());

        return stats;
    }

    @Override
    public List<PopularHashtagDTO> getPopularHashtagsFiltered(String ageGroup, String gender) {
        return dashboardMapper.getPopularHashtags(ageGroup, gender);
    }
}
