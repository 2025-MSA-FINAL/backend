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

    // 채팅 통계
    long countTotalChatRooms();

    // 신고 통계
    long countTotalReports();
    long countReportsByStatus(@Param("status") String status);

    // ===== 추가 통계 =====
    List<UserDemographicsDTO> getUserDemographics();

    List<PopularHashtagDTO> getPopularHashtags(
            @Param("ageGroup") String ageGroup,
            @Param("gender") String gender
    );
    List<ReportCategoryStatsDTO> getReportCategoryStats();

    // 월별 신규 가입자 추이 추가
    List<MonthlyUserGrowthDTO> getMonthlyUserGrowth();

    // 조회수 분석 추가(최근 7일, 히트맵 : 요일 × 시간대 , 주간 TOP10)
    List<DailyViewStatsDTO> getDailyViews(@Param("days") int days);

    List<ViewHeatmapDTO> getViewHeatmap();

    List<PopularPopupWeeklyDTO> getWeeklyTopPopups();

    List<ViewDetailPopupDTO> getViewDetailTopPopups(
            @Param("date") String date,
            @Param("hour") int hour);

    List<ViewDetailGenderDTO> getViewDetailGender(
            @Param("date") String date,
            @Param("hour") int hour);

    List<ViewDetailAgeDTO> getViewDetailAge(
            @Param("date") String date,
            @Param("hour") int hour);

    int getViewDetailTotalViews(
            @Param("date") String date,
            @Param("hour") int hour);





}
