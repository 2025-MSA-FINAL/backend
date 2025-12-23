package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.CategoryValidationDTO;
import com.popspot.popupplatform.dto.admin.HashtagCategoryStatsDTO;
import com.popspot.popupplatform.mapper.admin.AdminCategoryAnalysisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryAnalysisServiceImpl
        implements AdminCategoryAnalysisService {

    private final AdminCategoryAnalysisMapper mapper;

    @Override
    public List<CategoryValidationDTO> getCategoryValidationStats() {
        return mapper.validateCategoryHashtags();
    }


    @Override
    public CategoryValidationDTO getCategoryValidationStats(String category) {
        CategoryValidationDTO dto =
                mapper.validateCategoryHashtagsByCategory(category);

        if (dto != null && dto.getTotalTags() > 0) {
            dto.setAccuracy(
                    (double) dto.getMatchedTags() / dto.getTotalTags()
            );
        }
        return dto;
    }



    @Override
    public List<HashtagCategoryStatsDTO> getHashtagStatsByCategory(String category) {
        return mapper.findHashtagsByCategory(category);
    }
}
