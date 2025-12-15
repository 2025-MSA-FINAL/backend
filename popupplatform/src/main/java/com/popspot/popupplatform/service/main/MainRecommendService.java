package com.popspot.popupplatform.service.main;

import com.popspot.popupplatform.dto.main.MainRecommendResponse;

public interface MainRecommendService {
    /**
     * 로그인 여부에 따라
     * - 비로그인: 인기 추천
     * - 로그인: AI 추천
     */
    MainRecommendResponse getRecommendedPopups(Long userId, int limit);
}
