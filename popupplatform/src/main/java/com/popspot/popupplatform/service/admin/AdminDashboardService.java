package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.DashboardStatsDTO;
import com.popspot.popupplatform.dto.admin.KpiStatsDTO;
import com.popspot.popupplatform.dto.admin.PopularHashtagDTO;
import com.popspot.popupplatform.dto.admin.ViewDetailResponseDTO;

import java.util.List;

public interface AdminDashboardService {
    DashboardStatsDTO getDashboardStats();
    List<PopularHashtagDTO> getPopularHashtagsFiltered(String ageGroup, String gender);
    ViewDetailResponseDTO getViewDetail(String date, int hour);
}
