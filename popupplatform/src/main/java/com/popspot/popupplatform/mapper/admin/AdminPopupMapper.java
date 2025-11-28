package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminPopupMapper {

    // 팝업스토어 목록
    List<PopupStoreListDTO> findPopupList(PageRequestDTO pageRequest);
    long countPopupList();

    // 승인 대기 목록
    List<PopupStoreListDTO> findPendingPopupList(PageRequestDTO pageRequest);
    long countPendingPopupList();

    // 팝업스토어 상세
    PopupStoreListDTO findPopupById(@Param("popId") Long popId);

    // 승인/반려 상태 변경
    int updateModerationStatus(@Param("popId") Long popId, @Param("status") Boolean status);

    // 팝업스토어 상태 변경
    int updatePopupStatus(@Param("popId") Long popId, @Param("status") String status);

    // 팝업스토어 삭제 (soft delete)
    int deletePopup(@Param("popId") Long popId);

    // 팝업스토어 검색
    List<PopupStoreListDTO> searchPopups(@Param("keyword") String keyword, @Param("pageRequest") PageRequestDTO pageRequest);
    long countSearchPopups(@Param("keyword") String keyword);
}
