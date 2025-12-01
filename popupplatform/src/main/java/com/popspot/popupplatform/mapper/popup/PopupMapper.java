package com.popspot.popupplatform.mapper.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
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
            @Param("limit") int limit,
            @Param("keyword") String keyword,
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

    //팝업 단건 상세 조회
    Optional<PopupStore> selectPopupDetail(@Param("popId") Long popId);

    //팝업 상세 이미지 리스트 조회
    List<String> selectPopupImages(@Param("popId") Long popId);

    //팝업 해시태그 리스트 조회
    List<String> selectPopupHashtags(@Param("popId") Long popId);


}