package com.popspot.popupplatform.mapper.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.popup.response.PopupNearbyItemResponse;
import com.popspot.popupplatform.dto.user.report.UserPersonaPopupCard;
import com.popspot.popupplatform.dto.user.report.UserPopupEventDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface PopupMapper {

    //팝업 저장
    void insertPopup(PopupStore popupStore);

    //해시태그 이름으로 조회
    Optional<Long> findHashtagIdByName(@Param("hashName") String hashName);

    //해시태그 저장
    void insertHashtag(@Param("hashName") String hashName);

    //팝업-해시태그 연결
    void insertPopupHashtag(@Param("popId") Long popId,
                            @Param("hashId") Long hashId);

    //상세 이미지 저장 (1:N)
    void insertPopupImage(@Param("popId") Long popId,
                          @Param("imageUrl") String imageUrl,
                          @Param("order") int order);


    //팝업 목록 기본 조회 (커서 + 개수 기반)
    List<PopupStore> selectPopupList(
            @Param("cursorId") Long cursorId,
            @Param("cursorEndDate") LocalDateTime cursorEndDate,
            @Param("cursorViewCount") Long cursorViewCount,
            @Param("cursorStatusGroup") Integer cursorStatusGroup,
            @Param("limit") int limit,
            @Param("keywords") List<String> keywords,
            @Param("regions") List<String> regions,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("sort") String sort
    );

    //팝업 존재 여부 확인 (삭제되지 않은 것만)
    boolean existsById(@Param("popId") Long popId);

    //오픈 예정 -> 진행 중 상태 변경
    int updateStatusToOngoing(@Param("now") LocalDateTime now);

    //진행 중/오픈 예정 -> 종료 상태 변경
    int updateStatusToEnded(@Param("now") LocalDateTime now);

    //조회수 1 증가
    void updateViewCount(@Param("popId") Long popId);

    //유저가 해당 팝업을 최근 1시간 내에 조회했는지 확인
    boolean existsViewHistoryRecent(@Param("popId") Long popId, @Param("userId") Long userId);

    //조회 기록 저장 (POPUP_VIEWED 테이블)
    void insertViewHistory(@Param("popId") Long popId, @Param("userId") Long userId);

    //팝업 단건 상세 조회
    Optional<PopupStore> selectPopupDetail(@Param("popId") Long popId);

    //팝업 상세 이미지 리스트 조회
    List<String> selectPopupImages(@Param("popId") Long popId);

    //팝업 해시태그 리스트 조회
    List<String> selectPopupHashtags(@Param("popId") Long popId);

    //팝업 예약 시작 시간 조회
    LocalDateTime selectReservationStartTime(@Param("popId") Long popId);

    //AI 요약 업데이트 (비동기 처리가 끝난 후 실행)
    void updatePopupAiSummary(@Param("popId") Long popId,
                              @Param("summary") String summary);

    void updateIsReservation(@Param("popId") Long popId);

    // -------------------- [User Report] --------------------

    /**
     * 유저의 팝업 이용 이벤트(조회/찜/예약)를 모두 가져온다.
     * VIEW / WISHLIST / RESERVATION 을 UNION 해서 한 리스트로 반환.
     */
    List<UserPopupEventDto> selectUserPopupEvents(@Param("userId") Long userId);

    /**
     * 유저가 찜한 팝업의 해시태그와 겹치는 해시태그를 가진
     * (ENDED가 아닌) 팝업들을 추천용으로 조회.
     * - 겹치는 해시태그 수 + 조회수(pop_view_count) 기준 정렬
     */
    List<UserPersonaPopupCard> selectSimilarTastePopups(@Param("userId") Long userId,
                                                        @Param("limit") int limit);

    /**
     * 같은 성별 + 생년(연령대) 범위에 속한 유저들이
     * 많이 관심 가진 팝업(ENDED 아님)을 인기순으로 조회.
     */
    List<UserPersonaPopupCard> selectDemographicPopularPopups(@Param("gender") String gender,
                                                              @Param("birthYearStart") int birthYearStart,
                                                              @Param("birthYearEnd") int birthYearEnd,
                                                              @Param("limit") int limit);


    /**
     * 내 주변 팝업 조회
     * 위/경도가 NOT NULL 인 팝업만 대상
     * 삭제되지 않은 팝업 + ENDED 아닌 팝업만
     * Haversine 공식을 이용해 거리 계산 (km)
     */
    List<PopupNearbyItemResponse> selectNearbyPopups(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("limit") Integer limit
    );

}