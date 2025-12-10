package com.popspot.popupplatform.service.report;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.report.ReportDetailDTO;
import com.popspot.popupplatform.dto.report.ReportListDTO;

import java.util.Map;

/**
 * 신고 관리 서비스
 */
public interface ReportService {

    /**
     * 신고 목록 조회 (페이지네이션 + 상태 필터 + 카테고리 필터)
     *
     * @param status 신고 상태
     * @param categoryId 신고 카테고리 ID
     * @param pageRequest 페이지 요청 정보
     * @return 신고 목록 페이지
     */
    PageDTO<ReportListDTO> getReportList(String status, Long categoryId, PageRequestDTO pageRequest);

    /**
     * 신고 상세 조회
     *
     * @param repId 신고 ID
     * @return 신고 상세 정보
     */
    ReportDetailDTO getReportDetail(Long repId);

    /**
     * 신고 상태 변경
     *
     * @param repId 신고 ID
     * @param status 변경할 상태
     * @return 성공 여부
     */
    boolean updateReportStatus(Long repId, String status);

    /**
     * 신고 통계 조회 (3단계)
     *
     * @return 상태별 신고 수
     */
    Map<String, Long> getReportStats();

    /**
     * 신고 검색 (키워드 + 상태 필터 + 카테고리 필터)
     *
     * @param keyword 검색어
     * @param status 신고 상태
     * @param categoryId 신고 카테고리 ID
     * @param pageRequest 페이지 요청 정보
     * @return 검색 결과 페이지
     */
    PageDTO<ReportListDTO> searchReports(String keyword, String status, Long categoryId, PageRequestDTO pageRequest);
}