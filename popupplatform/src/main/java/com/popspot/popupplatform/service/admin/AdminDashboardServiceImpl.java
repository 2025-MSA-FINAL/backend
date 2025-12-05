package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.*;
import com.popspot.popupplatform.mapper.admin.AdminDashboardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper dashboardMapper;

    /** 메모리 캐시 저장소 */
    private final Map<String, Object> cache = new ConcurrentHashMap<>();


    /* ============================================================
        1) 전체 대시보드 캐싱 (매일 새벽 3시)
       ============================================================ */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshDailyCache() {
        log.info(" [스케줄러] 전체 대시보드 캐시 갱신 시작");

        DashboardStatsDTO stats = loadDashboardStatsInternal(true);
        cache.put("dashboardStats", stats);

        log.info(" [스케줄러] 전체 대시보드 캐시 갱신 완료");
    }


    /* ============================================================
        2) 조회수 관련 통계만 3시간마다 갱신 (히트맵 + 최근 7일)
       ============================================================ */
    @Scheduled(cron = "0 0 */3 * * *")
    public void refreshViewCache() {
        log.info(" [스케줄러] 조회수 분석 데이터 갱신 시작");

        DashboardStatsDTO cached = (DashboardStatsDTO) cache.get("dashboardStats");
        if (cached == null) {
            cached = loadDashboardStatsInternal(true);
        }

        // 갱신 대상: dailyViews + viewHeatmap + weeklyTopPopups
        List<DailyViewStatsDTO> daily = dashboardMapper.getDailyViews(7);
        cached.setDailyViews(daily);
        cached.setViewHeatmap(aggregateHeatmap(daily));
        cached.setWeeklyTopPopups(dashboardMapper.getWeeklyTopPopups());

        cache.put("dashboardStats", cached);

        log.info(" [스케줄러] 조회수 분석 데이터 갱신 완료");
    }


    /* ============================================================
        API에서 캐시 사용
       ============================================================ */
    @Override
    public DashboardStatsDTO getDashboardStats() {

        if (!cache.containsKey("dashboardStats")) {
            log.info(" 캐시 없음 → 즉시 생성");
            cache.put("dashboardStats", loadDashboardStatsInternal(true));
        }

        return (DashboardStatsDTO) cache.get("dashboardStats");
    }


    /* ============================================================
        실제 DB 조회 로직
         full = true → 전체 통계 전부 로딩
         full = false → 조회수 관련만
       ============================================================ */
    private DashboardStatsDTO loadDashboardStatsInternal(boolean full) {

        DashboardStatsDTO dto = new DashboardStatsDTO();

        if (full) {
            // 기본 유저 통계
            dto.setTotalUsers(dashboardMapper.countTotalUsers());
            dto.setNewUsersToday(dashboardMapper.countNewUsersToday());
            dto.setNewUsersThisWeek(dashboardMapper.countNewUsersThisWeek());
            dto.setNewUsersThisMonth(dashboardMapper.countNewUsersThisMonth());

            // 팝업 통계
            dto.setTotalPopupStores(dashboardMapper.countTotalPopups());
            dto.setActivePopupStores(dashboardMapper.countActivePopups());
            dto.setPendingApproval(dashboardMapper.countPendingApprovalPopups());
            dto.setEndingSoon(dashboardMapper.countEndingSoonPopups());

            //채팅방 통계
            dto.setTotalChatRooms(dashboardMapper.countTotalChatRooms());

            // 신고 통계
            dto.setTotalReports(dashboardMapper.countTotalReports());
            dto.setPendingReports(dashboardMapper.countReportsByStatus("pending"));
            dto.setApprovedReports(dashboardMapper.countReportsByStatus("approved"));
            dto.setResolvedReports(dashboardMapper.countReportsByStatus("resolved"));
            dto.setRejectedReports(dashboardMapper.countReportsByStatus("rejected"));

            // 성별/연령
            dto.setUserDemographics(dashboardMapper.getUserDemographics());

            // 인기 해시태그
            dto.setPopularHashtags(dashboardMapper.getPopularHashtags(null, null));

            // 신고 카테고리
            dto.setReportCategoryStats(dashboardMapper.getReportCategoryStats());

            // 월별 신규 유저
            dto.setMonthlyUserGrowth(dashboardMapper.getMonthlyUserGrowth());
        }

        // ===== 조회수 분석 =====
        List<DailyViewStatsDTO> daily = dashboardMapper.getDailyViews(7);
        dto.setDailyViews(daily);
        dto.setViewHeatmap(aggregateHeatmap(daily));

        // 주간 TOP10
        dto.setWeeklyTopPopups(dashboardMapper.getWeeklyTopPopups());

        return dto;
    }


    /* ============================================================
        날짜 × 시간대 히트맵 생성기
       ============================================================ */
    private List<ViewHeatmapDTO> aggregateHeatmap(List<DailyViewStatsDTO> raw) {

        DateTimeFormatter full = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter label = DateTimeFormatter.ofPattern("M/d(E)", Locale.KOREAN);

        // 기본 틀: 최근 7일 × 24시간
        Map<String, ViewHeatmapDTO> map = new LinkedHashMap<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dayLabel = date.format(label);
            String fullDate = date.format(full);

            for (int hour = 0; hour < 24; hour++) {
                String key = fullDate + "_" + hour;
                map.put(key, new ViewHeatmapDTO(dayLabel, fullDate, hour, 0));
            }
        }

        // 실제 조회수 적용
        for (DailyViewStatsDTO v : raw) {
            if (v.getFullDate() == null) {  //  fullDate만 체크
                continue;
            }

            String key = v.getFullDate() + "_" + v.getHour();
            if (map.containsKey(key)) {
                map.get(key).setViews(v.getViews());
            }
        }

        return new ArrayList<>(map.values());
    }



    @Override
    public ViewDetailResponseDTO getViewDetail(String date, int hour) {

        ViewDetailResponseDTO dto = new ViewDetailResponseDTO();

        dto.setFullDate(date);
        dto.setHour(hour);

        dto.setTotalViews(
                dashboardMapper.getViewDetailTotalViews(date, hour)
        );

        dto.setTopPopups(
                dashboardMapper.getViewDetailTopPopups(date, hour)
        );

        dto.setGenderStats(
                dashboardMapper.getViewDetailGender(date, hour)
        );

        dto.setAgeStats(
                dashboardMapper.getViewDetailAge(date, hour)
        );

        return dto;
    }


    // =========================================
    // 해시태그 필터 API
    // =========================================
    @Override
    public List<PopularHashtagDTO> getPopularHashtagsFiltered(String ageGroup, String gender) {
        return dashboardMapper.getPopularHashtags(ageGroup, gender);
    }
}