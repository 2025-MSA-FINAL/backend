package com.popspot.popupplatform.mapper.report;

import com.popspot.popupplatform.dto.report.ReportDetailDTO;
import com.popspot.popupplatform.dto.report.ReportListDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 신고 관리 Mapper
 */
@Mapper
public interface ReportMapper {

    // 기존 메서드들
    List<ReportListDTO> findAllReports();
    ReportDetailDTO findReportById(Long repId);
    List<String> findReportImages(Long repId);
    int updateReportStatus(@Param("repId") Long repId, @Param("status") String status);

    /**
     * 상태별 신고 수 조회
     * @param status "approved" 요청 시 approved + resolved 합산
     */
    long countByStatus(@Param("status") String status);

    /**
     * 전체 신고 목록 조회 (페이지네이션 + 카테고리 필터)
     * @param categoryId 신고 카테고리 ID (null이면 전체)
     */
    List<ReportListDTO> findAllReportsWithPagination(
            @Param("categoryId") Long categoryId,
            @Param("pageRequest") PageRequestDTO pageRequest
    );

    /**
     * 전체 신고 수 조회 (카테고리 필터)
     * @param categoryId 신고 카테고리 ID (null이면 전체)
     */
    long countAllReports(@Param("categoryId") Long categoryId);

    /**
     * 상태별 신고 목록 조회 (카테고리 필터 포함)
     * @param status "approved" 요청 시 approved + resolved 모두 포함
     * @param categoryId 신고 카테고리 ID (null이면 전체)
     */
    List<ReportListDTO> findReportsByStatus(
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("pageRequest") PageRequestDTO pageRequest
    );

    /**
     * 상태별 신고 수 조회 (카테고리 필터 포함)
     * @param status "approved" 요청 시 approved + resolved 합산
     * @param categoryId 신고 카테고리 ID (null이면 전체)
     */
    long countReportsByStatus(
            @Param("status") String status,
            @Param("categoryId") Long categoryId
    );

    /**
     * 검색 (키워드 + searchType + 상태 필터 + 카테고리 필터)
     * @param keyword 검색어
     * @param searchType 검색 타입 (reporterName, reporterNickname, targetName, categoryName, all)
     * @param status "approved" 요청 시 approved + resolved 모두 포함
     * @param categoryId 신고 카테고리 ID (null이면 전체)
     */
    List<ReportListDTO> searchReports(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("pageRequest") PageRequestDTO pageRequest
    );

    /**
     * 검색 결과 수 조회 (카테고리 필터 포함)
     * @param keyword 검색어
     * @param searchType 검색 타입
     * @param status "approved" 요청 시 approved + resolved 합산
     * @param categoryId 신고 카테고리 ID (null이면 전체)
     */
    long countSearchReports(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("status") String status,
            @Param("categoryId") Long categoryId
    );
}