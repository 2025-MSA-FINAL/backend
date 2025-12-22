package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminPopupMapper {

    /**
     * 팝업스토어 목록 조회 (통합 검색/필터)
     * @param pageRequest 페이징 정보
     * @param keyword 검색어 (optional)
     * @param status 팝업 상태 (optional)
     * @param moderation 승인 상태 (optional)
     * @param deletedFilter  삭제 필터 (active/deleted/all)
     */
    List<PopupStoreListDTO> findPopupList(
            @Param("pageRequest") PageRequestDTO pageRequest,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("moderation") String moderation,
            @Param("deletedFilter") String deletedFilter
    );

    /**
     * 팝업스토어 총 개수 (통합 검색/필터)
     * @param deletedFilter  삭제 필터 추가
     */
    long countPopupList(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("moderation") String moderation,
            @Param("deletedFilter") String deletedFilter
    );
    
    /** 팝업 승인상태 변경이력 **/
    int insertPopupModeration(
            @Param("popId") Long popId,
            @Param("adminId") Long adminId,
            @Param("status") String status,
            @Param("comment") String comment
    );

    /**
     * 팝업스토어 상세 조회
     */
    PopupStoreListDTO findPopupById(@Param("popId") Long popId);


    // AI / 도메인용 팝업스토어 상세 조회
    PopupStore findPopupEntityById(@Param("popId") Long popId);
    List<PopupStoreListDTO> findPopupsByIds(@Param("list") List<Long> popIds);

    /**
     *  승인/반려/대기 상태 변경 (자유롭게 변경 가능)
     * @param popId 팝업 ID
     * @param status null=대기, true=승인, false=거절
     */
    int updateModerationStatus(@Param("popId") Long popId, @Param("status") Boolean status);

    /**
     * 팝업스토어 상태 변경 (UPCOMING/ONGOING/ENDED)
     */
    int updatePopupStatus(@Param("popId") Long popId, @Param("status") String status);

    /**
     * 팝업스토어 삭제 (soft delete)
     */
    int deletePopup(@Param("popId") Long popId);

    /**
     *  팝업스토어 복구 (삭제 취소)
     */
    int restorePopup(@Param("popId") Long popId);

    /**
     * 전체 통계 조회 (필터 무관)
     */
    Map<String, Object> getPopupStats();
}