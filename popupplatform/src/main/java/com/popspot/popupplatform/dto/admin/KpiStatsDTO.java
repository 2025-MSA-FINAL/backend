package com.popspot.popupplatform.dto.admin;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiStatsDTO {

    private long totalUsers;
    private long newUsersToday;      // 오늘 신규 유저
    private long newUsersThisWeek;   // 이번주 신규 유저

    private long totalPopups;
    private long activePopups;       // 활성 상태 팝업 (status='active')
    private long pendingPopups;      // 승인 대기 (moderation=null or waiting)

    private long totalReports;
    private long pendingReports;

    private long totalChatRooms;     // 전체 채팅방
    private long endingSoon;         // 종료 임박 팝업 (7일 이내)
}