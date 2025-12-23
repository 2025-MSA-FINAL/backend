package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.CategoryValidationDTO;
import com.popspot.popupplatform.dto.admin.HashtagCategoryStatsDTO;

import java.util.List;

public interface AdminCategoryAnalysisService {
    //관리자 대시보드용 (전체)
    List<CategoryValidationDTO> getCategoryValidationStats();

    //Admin AI 리포트용 (단건)
    CategoryValidationDTO getCategoryValidationStats(String category);
    List<HashtagCategoryStatsDTO> getHashtagStatsByCategory(String category);
}
