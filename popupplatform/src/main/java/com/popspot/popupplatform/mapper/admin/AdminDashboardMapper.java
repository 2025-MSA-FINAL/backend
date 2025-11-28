package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    // 기본 유저 통계
    long countTotalUsers();
    long countNewUsersToday();
    long countNewUsersThisWeek();
    long countNewUsersThisMonth();

    // 팝업스토어 통계
    long countTotalPopups();
    long countActivePopups();
    long countPendingApprovalPopups();
    long countEndingSoonPopups();

    // 신고 통계
    long countTotalReports();
    long countReportsByStatus(@Param("status") String status);

    // ===== 필수 통계 쿼리 =====

    // 1. 성별/연령별 유저 분포
    List<UserDemographicsDTO> getUserDemographics();

    // 2. 이번 주 인기 팝업 Top 10 (조회수 기준)
    List<PopularPopupDTO> getTopPopupsThisWeek();

    // 3. 인기 해시태그 (찜 기준, 필터링 가능)
    List<PopularHashtagDTO> getPopularHashtags(
            @Param("ageGroup") String ageGroup,
            @Param("gender") String gender);

    // 4. 카테고리별 신고 건수
    List<ReportCategoryStatsDTO> getReportCategoryStats();
}