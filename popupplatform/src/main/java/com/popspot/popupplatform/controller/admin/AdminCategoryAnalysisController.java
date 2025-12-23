package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.CategoryValidationDTO;
import com.popspot.popupplatform.dto.admin.HashtagCategoryStatsDTO;
import com.popspot.popupplatform.service.admin.AdminCategoryAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category-analysis")
@RequiredArgsConstructor
public class AdminCategoryAnalysisController {

    private final AdminCategoryAnalysisService service;

    @GetMapping("/validation")
    public List<CategoryValidationDTO> getCategoryValidation() {
        return service.getCategoryValidationStats();
    }

    @GetMapping("/hashtags")
    public List<HashtagCategoryStatsDTO> getHashtags(
            @RequestParam String category
    ) {
        return service.getHashtagStatsByCategory(category);
    }
}
