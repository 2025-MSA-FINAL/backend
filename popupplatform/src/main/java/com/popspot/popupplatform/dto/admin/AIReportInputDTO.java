package com.popspot.popupplatform.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 리포트 생성을 위해 Service Layer로 전달되는 원시 데이터 셋 DTO.
 */
@Data
@Builder
public class AIReportInputDTO {

    // 리포트 기간 정보
    private LocalDate startDate;
    private LocalDate endDate;

    // 핵심 KPI
    private long totalUsers;           // 전체 사용자 수
    private long newSignups;           // 신규 가입자 수
    private long totalPopups;          // 등록된 팝업 수
    private long totalReservations;    // 총 예약 건수

    // 인구통계 및 행동 데이터 (예시)
    private List<UserDemographicsDTO> ageGenderStats; // 연령/성별 통계
    private List<PopularPopupDTO> popularPopups;      // 인기 팝업 목록
    private List<PopularHashtagDTO> popularHashtags;  // 인기 해시태그 목록

    // 외부 트렌드 데이터 (가정)
    private ExternalTrendDTO externalTrendData;

    // 기타 필요한 통계 데이터 목록...
}
