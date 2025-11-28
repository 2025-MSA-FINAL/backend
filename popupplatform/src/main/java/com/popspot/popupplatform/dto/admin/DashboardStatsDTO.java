package com.popspot.popupplatform.dto.admin;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsDTO {
    // 기본 유저 통계
    private long totalUsers;           // 전체 유저 수
    private long newUsersToday;        // 오늘 가입한 유저 수
    private long newUsersThisWeek;     // 이번 주 가입한 유저 수
    private long newUsersThisMonth;    // 이번 달 가입한 유저 수

    // 팝업스토어 통계
    private long totalPopupStores;     // 전체 팝업스토어 수
    private long activePopupStores;    // 진행 중인 팝업스토어 수
    private long pendingApproval;      // 승인 대기 중인 팝업스토어 수
    private long endingSoon;           // 7일 이내 종료 예정 팝업스토어 수

    // 신고 통계
    private long totalReports;         // 전체 신고 수
    private long pendingReports;       // 처리 대기 중인 신고 수
    private long approvedReports;      // 승인된 신고 수
    private long resolvedReports;      // 해결된 신고 수
    private long rejectedReports;      // 반려된 신고 수

    // ===== 필수 추가 통계 =====

    // 1. 성별, 연령별 유저 분포
    private List<UserDemographicsDTO> userDemographics;

    // 2. 이번 주 인기 팝업 Top 10 (조회수 기준)
    private List<PopularPopupDTO> topPopupsThisWeek;

    // 3. 인기 해시태그 (찜 기준)
    private List<PopularHashtagDTO> popularHashtags;

    // 4. 카테고리별 신고 건수
    private List<ReportCategoryStatsDTO> reportCategoryStats;
}