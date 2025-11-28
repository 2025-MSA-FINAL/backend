package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

public interface AdminPopupService {
    PageDTO<PopupStoreListDTO> getPopupList(PageRequestDTO pageRequest);
    PageDTO<PopupStoreListDTO> getPendingPopupList(PageRequestDTO pageRequest);
    PopupStoreListDTO getPopupDetail(Long popId);
    boolean updateModerationStatus(Long popId, Boolean status);
    boolean updatePopupStatus(Long popId, String status);
    boolean deletePopup(Long popId);
    PageDTO<PopupStoreListDTO> searchPopups(String keyword, PageRequestDTO pageRequest);
}
