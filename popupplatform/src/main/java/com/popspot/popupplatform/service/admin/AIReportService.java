package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AIReportResponseDTO;

public interface AIReportService {

    /**
     * 월간 운영 데이터를 수집하고, AI 분석을 통해 리포트를 생성하며, 이를 DB에 저장 후 응답 DTO를 반환합니다.
     * @return AIReportResponseDTO
     */
    AIReportResponseDTO generateMonthlyReport(String startDate, String endDate);

    /**
     * 데이터베이스에서 가장 최근에 저장된 AI 리포트 결과를 조회하여 반환합니다.
     * @return AIReportResponseDTO
     */
    AIReportResponseDTO getLatestAIReport();
}


