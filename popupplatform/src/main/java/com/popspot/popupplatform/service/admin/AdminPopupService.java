package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminPopupDetailResponseDTO;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

import java.util.Map;

public interface AdminPopupService {

    /**
     * 팝업스토어 목록 조회 (통합 검색/필터)
     */
    PageDTO<PopupStoreListDTO> getPopupList(
            PageRequestDTO pageRequest,
            String keyword,
            String status,
            String moderation,
            String deletedFilter
    );


    /**
     *  추가: 관리자용 팝업 상세 조회 (매니저 뷰)
     * - 대기 중 팝업 포함 모든 팝업 상세 조회 가능
     * - 매니저가 보는 것과 동일한 상세 정보 제공
     * @param popId 팝업 ID
     * @return 팝업 상세 정보
     */
    AdminPopupDetailResponseDTO getPopupDetailForAdmin(Long popId);

    /**
     * 승인/반려/대기 상태 변경 (자유롭게 변경 가능)
     */
    boolean updateModerationStatus(Long popId, Boolean status, String reason);

    /**
     * 팝업스토어 상태 변경
     */
    boolean updatePopupStatus(Long popId, String status);

    /**
     * 팝업스토어 삭제 (사유 포함)
     */
    boolean deletePopup(Long popId, String reason);

    /**
     * 팝업스토어 복구 (삭제 취소)
     */
    boolean restorePopup(Long popId);

    /**
     * 전체 통계 조회 (필터 무관)
     */
    Map<String, Object> getPopupStats();
}