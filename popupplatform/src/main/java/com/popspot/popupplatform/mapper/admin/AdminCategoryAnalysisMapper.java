package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.CategoryValidationDTO;
import com.popspot.popupplatform.dto.admin.HashtagCategoryStatsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminCategoryAnalysisMapper {

    List<CategoryValidationDTO> validateCategoryHashtags();

    CategoryValidationDTO validateCategoryHashtagsByCategory(String category);

    List<HashtagCategoryStatsDTO> findHashtagsByCategory(String category);

}
