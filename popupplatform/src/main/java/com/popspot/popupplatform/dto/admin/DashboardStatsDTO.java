package com.popspot.popupplatform.dto.admin;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsDTO {
    // 기본 유저 통계
    private long totalUsers;
    private long newUsersToday;
    private long newUsersThisWeek;
    private long newUsersThisMonth;

    // 팝업스토어 통계
    private long totalPopupStores;
    private long activePopupStores;
    private long pendingApproval;
    private long endingSoon;

    //채팅방 통계
    private Long totalChatRooms;

    // 신고 통계
    private long totalReports;
    private long pendingReports;
    private long approvedReports;
    private long resolvedReports;
    private long rejectedReports;

    // ===== 필수 추가 통계 =====

    // 1. 성별/연령별 유저 분포
    private List<UserDemographicsDTO> userDemographics;

    // 2. 이번 주 인기 팝업
    private List<PopularPopupDTO> topPopupsThisWeek;

    // 3. 인기 해시태그
    private List<PopularHashtagDTO> popularHashtags;

    // 4. 카테고리별 신고건수
    private List<ReportCategoryStatsDTO> reportCategoryStats;

    // 5. 월별 신규 가입자 추이
    private List<MonthlyUserGrowthDTO> monthlyUserGrowth;

    // 6. 조회수 분석 -----
    private List<DailyViewStatsDTO> dailyViews;            // 최근 7일
    private List<ViewHeatmapDTO> viewHeatmap; // day, fullDate, hour, views

    // 7. 주간 인기 팝업 필드
    private List<PopularPopupWeeklyDTO> weeklyTopPopups;  // 주간 TOP10

    // 8. 해시태그 통계
    //private List<HashtagCategoryStatsDTO> hashtagCategoryStats;

    // 9. 카테고리 분석 데이터 Google Trends API 사용
    //private List<CategoryValidationDTO> categoryValidationStats;


}
