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

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    public PageDTO<ReportListDTO> getReportList(String status, PageRequestDTO pageRequest) {
        List<ReportListDTO> reports;
        long total;

        if (status != null && !status.isEmpty()) {
            reports = reportMapper.findReportsByStatus(status, pageRequest);
            total = reportMapper.countReportsByStatus(status);
        } else {
            reports = reportMapper.findAllReportsWithPagination(pageRequest);
            total = reportMapper.countAllReports();
        }

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
        stats.put("pending", reportMapper.countByStatus("pending"));
        stats.put("approved", reportMapper.countByStatus("approved"));
        stats.put("resolved", reportMapper.countByStatus("resolved"));
        stats.put("rejected", reportMapper.countByStatus("rejected"));
        return stats;
    }

    @Override
    public PageDTO<ReportListDTO> searchReports(String keyword, PageRequestDTO pageRequest) {
        List<ReportListDTO> reports = reportMapper.searchReports(keyword, pageRequest);
        long total = reportMapper.countSearchReports(keyword);
        return new PageDTO<>(reports, pageRequest.getPage(), pageRequest.getSize(), total);
    }
}
