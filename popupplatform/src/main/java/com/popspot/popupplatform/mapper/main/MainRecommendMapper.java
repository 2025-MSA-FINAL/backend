package com.popspot.popupplatform.mapper.main;

import com.popspot.popupplatform.dto.main.MainRecommendPopupDto;
import com.popspot.popupplatform.dto.main.UserTasteProfileDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MainRecommendMapper {

    /* =========================
       비로그인: 인기 추천
       ========================= */
    List<MainRecommendPopupDto> selectPopularPopups(
            @Param("limit") int limit
    );

    /* =========================
       로그인: 유저 취향 프로필
       (최근본 + 찜 + 태그 Top)
       ========================= */
    UserTasteProfileDto selectUserTasteProfile(
            @Param("userId") Long userId
    );

    /* =========================
       로그인: AI 후보군 (최대 N개)
       ========================= */
    List<MainRecommendPopupDto> selectAiCandidates(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    /* =========================
       AI 결과 popId로
       순서 유지 조회
       ========================= */
    List<MainRecommendPopupDto> selectPopupsByIdsInOrder(
            @Param("ids") List<Long> ids
    );
}