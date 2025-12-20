package com.popspot.popupplatform.service.report;

import com.popspot.popupplatform.dto.report.ReportDetailDTO;
import com.popspot.popupplatform.dto.report.ReportListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.report.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 신고 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    public PageDTO<ReportListDTO> getReportList(String status, Long categoryId, PageRequestDTO pageRequest) {
        log.info("=== getReportList (검색 없음) ===");
        log.info("status: {}, categoryId: {}, page: {}, size: {}", status, categoryId, pageRequest.getPage(), pageRequest.getSize());

        List<ReportListDTO> reports;
        long total;

        if (status != null && !status.isEmpty()) {
            reports = reportMapper.findReportsByStatus(status, categoryId, pageRequest);
            total = reportMapper.countReportsByStatus(status, categoryId);
        } else {
            reports = reportMapper.findAllReportsWithPagination(categoryId, pageRequest);
            total = reportMapper.countAllReports(categoryId);
        }

        log.info("결과: {} / {}", reports.size(), total);
        return new PageDTO<>(reports, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public ReportDetailDTO getReportDetail(Long repId) {
        ReportDetailDTO detail = reportMapper.findReportById(repId);

        if (detail == null) {
            return null;
        }

        detail.setReportImages(reportMapper.findReportImages(repId));
        return detail;
    }

    @Override
    public boolean updateReportStatus(Long repId, String status) {
        return reportMapper.updateReportStatus(repId, status) > 0;
    }

    @Override
    public Map<String, Long> getReportStats() {
        Map<String, Long> stats = new HashMap<>();

        long pendingCount = reportMapper.countByStatus("pending");
        long approvedCount = reportMapper.countByStatus("approved");
        long rejectedCount = reportMapper.countByStatus("rejected");

        stats.put("pending", pendingCount);
        stats.put("approved", approvedCount);
        stats.put("rejected", rejectedCount);

        return stats;
    }

    @Override
    public PageDTO<ReportListDTO> searchReports(
            String keyword,
            String searchType,
            String status,
            Long categoryId,
            PageRequestDTO pageRequest) {

        log.info("=== searchReports (검색 있음) ===");
        log.info("keyword: [{}]", keyword);
        log.info("searchType: [{}]", searchType);
        log.info("status: {}", status);
        log.info("categoryId: {}", categoryId);
        log.info("page: {}, size: {}, offset: {}", pageRequest.getPage(), pageRequest.getSize(), pageRequest.getOffset());

        List<ReportListDTO> reports = reportMapper.searchReports(
                keyword,
                searchType,
                status,
                categoryId,
                pageRequest
        );

        long total = reportMapper.countSearchReports(
                keyword,
                searchType,
                status,
                categoryId
        );

        log.info("결과: {} / {}", reports.size(), total);

        if (!reports.isEmpty()) {
            log.info("첫 번째 결과 - repId: {}, repType: {}, categoryName: [{}], reporterName: [{}]",
                    reports.get(0).getRepId(),
                    reports.get(0).getRepType(),
                    reports.get(0).getCategoryName(),
                    reports.get(0).getReporterName());
        }

        return new PageDTO<>(reports, pageRequest.getPage(), pageRequest.getSize(), total);
    }
}