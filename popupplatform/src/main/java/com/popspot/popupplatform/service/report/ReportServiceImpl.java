package com.popspot.popupplatform.service.report;

import com.popspot.popupplatform.dto.report.ReportDetailDTO;
import com.popspot.popupplatform.dto.report.ReportListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.report.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 신고 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    public PageDTO<ReportListDTO> getReportList(String status, Long categoryId, PageRequestDTO pageRequest) {
        List<ReportListDTO> reports;
        long total;

        if (status != null && !status.isEmpty()) {
            // 상태 필터가 있는 경우 (+ 카테고리 필터)
            reports = reportMapper.findReportsByStatus(status, categoryId, pageRequest);
            total = reportMapper.countReportsByStatus(status, categoryId);
        } else {
            // 전체 목록 조회 (+ 카테고리 필터)
            reports = reportMapper.findAllReportsWithPagination(categoryId, pageRequest);
            total = reportMapper.countAllReports(categoryId);
        }

        return new PageDTO<>(reports, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public ReportDetailDTO getReportDetail(Long repId) {
        ReportDetailDTO detail = reportMapper.findReportById(repId);

        if (detail == null) {
            return null;
        }

        // 신고 이미지 추가
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

        // 3단계 통계
        long pendingCount = reportMapper.countByStatus("pending");

        // approved + resolved 통합 (XML에서 자동 합산)
        long approvedCount = reportMapper.countByStatus("approved");

        long rejectedCount = reportMapper.countByStatus("rejected");

        stats.put("pending", pendingCount);
        stats.put("approved", approvedCount);  // approved + resolved 합산
        stats.put("rejected", rejectedCount);

        return stats;
    }

    @Override
    public PageDTO<ReportListDTO> searchReports(String keyword, String status, Long categoryId, PageRequestDTO pageRequest) {
        // 검색 (키워드 + 상태 필터 + 카테고리 필터 동시 적용)
        List<ReportListDTO> reports = reportMapper.searchReports(keyword, status, categoryId, pageRequest);
        long total = reportMapper.countSearchReports(keyword, status, categoryId);

        return new PageDTO<>(reports, pageRequest.getPage(), pageRequest.getSize(), total);
    }
}