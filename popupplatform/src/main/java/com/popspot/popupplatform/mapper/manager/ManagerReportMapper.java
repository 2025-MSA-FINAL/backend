package com.popspot.popupplatform.mapper.manager;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ManagerReportMapper {

    // 1. KPI 조회
    Map<String, Object> selectKpiStats(@Param("popupId") Long popupId);

    // ==========================================
    // 2. Audience (예약자 기준 - RESERVATION 모드)
    // ==========================================
    List<Map<String, Object>> selectAudienceGender(@Param("popupId") Long popupId);
    List<Map<String, Object>> selectAudienceAge(@Param("popupId") Long popupId);
    List<Map<String, Object>> selectAudienceTime(@Param("popupId") Long popupId);

    // ==========================================
    // 2-1. Audience (찜 유저 기준 - WISHLIST 모드)
    // ==========================================
    List<Map<String, Object>> selectAudienceGenderByWishlist(@Param("popupId") Long popupId);
    List<Map<String, Object>> selectAudienceAgeByWishlist(@Param("popupId") Long popupId);
    List<Map<String, Object>> selectAudienceTimeByWishlist(@Param("popupId") Long popupId);

    // ==========================================
    // 5. Market Trend
    // ==========================================
    // 의미 있는 해시태그 추출
    List<String> selectMeaningfulHashtags(@Param("popupId") Long popupId);

    // 시장 전체 통계 조회
    Map<String, Object> selectMarketTrendStats(@Param("hashtag") String hashtag);

    // 내 팝업 성과 (예약 기준)
    Map<String, Object> selectMyPopupTrendStats(@Param("popupId") Long popupId, @Param("hashtag") String hashtag);

    // 내 팝업 성과 (찜 기준)
    Map<String, Object> selectMyPopupTrendStatsByWishlist(@Param("popupId") Long popupId, @Param("hashtag") String hashtag);
}