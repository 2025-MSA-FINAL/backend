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
     * @param deletedFilter  삭제 필터 (active/deleted/all)
     */
    PageDTO<PopupStoreListDTO> getPopupList(
            PageRequestDTO pageRequest,
            String keyword,
            String status,
            String moderation,
            String deletedFilter
    );

    /**
     * 팝업스토어 상세 조회
     */
    PopupStoreListDTO getPopupDetail(Long popId);

    /**
     *  승인/반려/대기 상태 변경 (자유롭게 변경 가능)
     * @param popId 팝업 ID
     * @param status null=대기, true=승인, false=거절
     * @return 성공 여부
     */
    boolean updateModerationStatus(Long popId, Boolean status, String reason);
    /**
     * 팝업스토어 상태 변경
     */
    boolean updatePopupStatus(Long popId, String status);

    /**
     *  팝업스토어 삭제 (사유 포함)
     * @param popId 팝업 ID
     * @param reason 삭제 사유 (optional)
     */
    boolean deletePopup(Long popId, String reason);

    /**
     *  팝업스토어 복구 (삭제 취소)
     * @param popId 팝업 ID
     * @return 성공 여부
     */
    boolean restorePopup(Long popId);

    /**
     * 전체 통계 조회 (필터 무관)
     */
    Map<String, Object> getPopupStats();
}