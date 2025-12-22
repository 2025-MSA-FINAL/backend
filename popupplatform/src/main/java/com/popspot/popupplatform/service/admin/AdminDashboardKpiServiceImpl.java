package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.KpiStatsDTO;
import com.popspot.popupplatform.mapper.admin.AdminDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardKpiServiceImpl
        implements AdminDashboardKpiService {

    private final AdminDashboardMapper dashboardMapper;

    @Override
    public KpiStatsDTO getKpiStats() {
        return KpiStatsDTO.builder()
                .totalUsers(dashboardMapper.countTotalUsers())
                .newUsersToday(dashboardMapper.countNewUsersToday())
                .newUsersThisWeek(dashboardMapper.countNewUsersThisWeek())

                .totalPopups(dashboardMapper.countTotalPopups())
                .activePopups(dashboardMapper.countActivePopups())
                .pendingPopups(dashboardMapper.countPendingApprovalPopups())

                .totalReports(dashboardMapper.countTotalReports())
                .pendingReports(dashboardMapper.countReportsByStatus("pending"))

                .totalChatRooms(dashboardMapper.countTotalChatRooms())
                .endingSoon(dashboardMapper.countEndingSoonPopups())

                .build();
    }
}