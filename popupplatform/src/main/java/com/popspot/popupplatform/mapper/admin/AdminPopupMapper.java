package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminPopupMapper {

    /**
     * 팝업스토어 목록 조회 (통합 검색/필터)
     * @param pageRequest 페이징 정보
     * @param keyword 검색어 (optional)
     * @param status 팝업 상태 (optional)
     * @param moderation 승인 상태 (optional)
     */
    List<PopupStoreListDTO> findPopupList(
            @Param("pageRequest") PageRequestDTO pageRequest,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("moderation") String moderation
    );

    /**
     * 팝업스토어 총 개수 (통합 검색/필터)
     */
    long countPopupList(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("moderation") String moderation
    );

    /**
     * 팝업스토어 상세 조회
     */
    PopupStoreListDTO findPopupById(@Param("popId") Long popId);

    /**
     * 승인/반려 상태 변경
     */
    int updateModerationStatus(@Param("popId") Long popId, @Param("status") Boolean status);

    /**
     * 팝업스토어 상태 변경
     */
    int updatePopupStatus(@Param("popId") Long popId, @Param("status") String status);

    /**
     * 팝업스토어 삭제 (soft delete)
     */
    int deletePopup(@Param("popId") Long popId);
}