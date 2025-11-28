package com.popspot.popupplatform.mapper.report;

import com.popspot.popupplatform.dto.report.ReportDetailDTO;
import com.popspot.popupplatform.dto.report.ReportListDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    // 기존 메서드들
    List<ReportListDTO> findAllReports();
    ReportDetailDTO findReportById(Long repId);
    List<String> findReportImages(Long repId);
    int updateReportStatus(@Param("repId") Long repId, @Param("status") String status);
    long countByStatus(@Param("status") String status);

    // 페이지네이션 추가
    List<ReportListDTO> findAllReportsWithPagination(PageRequestDTO pageRequest);
    long countAllReports();

    List<ReportListDTO> findReportsByStatus(@Param("status") String status, @Param("pageRequest") PageRequestDTO pageRequest);
    long countReportsByStatus(@Param("status") String status);

    // 검색
    List<ReportListDTO> searchReports(@Param("keyword") String keyword, @Param("pageRequest") PageRequestDTO pageRequest);
    long countSearchReports(@Param("keyword") String keyword);
}
