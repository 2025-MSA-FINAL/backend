package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

import java.util.Map;

public interface AdminPopupService {

    /**
     * 팝업스토어 목록 조회 (통합 검색/필터)
     * @param pageRequest 페이징 정보
     * @param keyword 검색어 (optional)
     * @param status 팝업 상태 (optional)
     * @param moderation 승인 상태 (optional)
     */
    PageDTO<PopupStoreListDTO> getPopupList(
            PageRequestDTO pageRequest,
            String keyword,
            String status,
            String moderation
    );

    /**
     * 팝업스토어 상세 조회
     */
    PopupStoreListDTO getPopupDetail(Long popId);

    /**
     * 승인/반려 상태 변경
     */
    boolean updateModerationStatus(Long popId, Boolean status);

    /**
     * 팝업스토어 상태 변경
     */
    boolean updatePopupStatus(Long popId, String status);

    /**
     * 팝업스토어 삭제
     */
    boolean deletePopup(Long popId);

    /**
     * 전체 통계 조회 (필터 무관)
     */
    Map<String, Object> getPopupStats();



}