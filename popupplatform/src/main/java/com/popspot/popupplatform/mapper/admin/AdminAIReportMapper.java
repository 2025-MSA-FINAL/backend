package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.domain.admin.AdminAIReport;
import com.popspot.popupplatform.domain.admin.AdminAIReportChart;
import com.popspot.popupplatform.dto.admin.AIReportResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface AdminAIReportMapper {
    // 1. 리포트 메인 저장
    int insertAIReport(AdminAIReport report);

    // 2. 리포트에 포함될 개별 차트들 저장
    Optional<AIReportResponseDTO> findLatestAIReport();

    //3. 차트 이미지 정보 저장
    void insertReportChart(AdminAIReportChart chart);
}