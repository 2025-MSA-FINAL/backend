package com.popspot.popupplatform.service.report;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.report.ReportDetailDTO;
import com.popspot.popupplatform.dto.report.ReportListDTO;

import java.util.Map;

public interface ReportService {

    // 페이지네이션이 적용된 신고 목록
    PageDTO<ReportListDTO> getReportList(String status, PageRequestDTO pageRequest);

    ReportDetailDTO getReportDetail(Long repId);

    boolean updateReportStatus(Long repId, String status);

    Map<String, Long> getReportStats();

    // 신고 검색
    PageDTO<ReportListDTO> searchReports(String keyword, PageRequestDTO pageRequest);
}