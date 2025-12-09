package com.popspot.popupplatform.mapper.manager;

import com.popspot.popupplatform.dto.popup.response.ManagerPopupDetailResponse;
import com.popspot.popupplatform.dto.user.response.ManagerReservationResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ManagerPopupMapper {

    /**
     * 1. 매니저용 팝업 상세 조회
     */
    Optional<ManagerPopupDetailResponse> selectPopupDetail(
            @Param("popId") Long popId,
            @Param("managerId") Long managerId // 내 팝업만 조회 가능하도록
    );

    /**
     * 2. 예약자 목록 조회 (페이징)
     */
    List<ManagerReservationResponse> selectReservations(
            @Param("popId") Long popId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 3. 예약자 전체 수 (페이징 계산용)
     */
    long countReservations(@Param("popId") Long popId);

    /**
     * 4. 팝업 기본 정보 수정
     */
    int updatePopup(
            @Param("popId") Long popId,
            @Param("managerId") Long managerId,
            @Param("request") com.popspot.popupplatform.dto.popup.request.ManagerPopupUpdateRequest request
    );

    //기존 이미지 삭제
    void deletePopupImages(@Param("popId") Long popId);
    //기존 해시태그 삭제
    void deletePopupHashtags(@Param("popId") Long popId);

    /**
     * 5. 팝업 삭제 (Soft Delete)
     */
    int deletePopup(@Param("popId") Long popId, @Param("managerId") Long managerId);



}