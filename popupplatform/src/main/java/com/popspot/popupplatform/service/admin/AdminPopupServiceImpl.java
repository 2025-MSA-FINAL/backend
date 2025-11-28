package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.admin.AdminPopupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPopupServiceImpl implements AdminPopupService {

    private final AdminPopupMapper popupMapper;

    @Override
    public PageDTO<PopupStoreListDTO> getPopupList(PageRequestDTO pageRequest) {
        List<PopupStoreListDTO> popups = popupMapper.findPopupList(pageRequest);
        long total = popupMapper.countPopupList();
        return new PageDTO<>(popups, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public PageDTO<PopupStoreListDTO> getPendingPopupList(PageRequestDTO pageRequest) {
        List<PopupStoreListDTO> popups = popupMapper.findPendingPopupList(pageRequest);
        long total = popupMapper.countPendingPopupList();
        return new PageDTO<>(popups, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public PopupStoreListDTO getPopupDetail(Long popId) {
        return popupMapper.findPopupById(popId);
    }

    @Override
    public boolean updateModerationStatus(Long popId, Boolean status) {
        return popupMapper.updateModerationStatus(popId, status) > 0;
    }

    @Override
    public boolean updatePopupStatus(Long popId, String status) {
        return popupMapper.updatePopupStatus(popId, status) > 0;
    }

    @Override
    public boolean deletePopup(Long popId) {
        return popupMapper.deletePopup(popId) > 0;
    }

    @Override
    public PageDTO<PopupStoreListDTO> searchPopups(String keyword, PageRequestDTO pageRequest) {
        List<PopupStoreListDTO> popups = popupMapper.searchPopups(keyword, pageRequest);
        long total = popupMapper.countSearchPopups(keyword);
        return new PageDTO<>(popups, pageRequest.getPage(), pageRequest.getSize(), total);
    }
}
