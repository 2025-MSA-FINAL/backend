package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.admin.AdminPopupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPopupServiceImpl implements AdminPopupService {

    private final AdminPopupMapper popupMapper;

    /**
     * 팝업스토어 목록 조회 (통합 검색/필터)
     */
    @Override
    public PageDTO<PopupStoreListDTO> getPopupList(
            PageRequestDTO pageRequest,
            String keyword,
            String status,
            String moderation) {

        // 목록 조회
        List<PopupStoreListDTO> content = popupMapper.findPopupList(
                pageRequest, keyword, status, moderation
        );

        // 총 개수 조회
        long total = popupMapper.countPopupList(keyword, status, moderation);

        // PageDTO 생성
        return new PageDTO<>(
                content,
                pageRequest.getPage(),
                pageRequest.getSize(),
                total
        );
    }

    /**
     * 팝업스토어 상세 조회
     */
    @Override
    public PopupStoreListDTO getPopupDetail(Long popId) {
        return popupMapper.findPopupById(popId);
    }

    /**
     * 승인/반려 상태 변경
     */
    @Override
    @Transactional
    public boolean updateModerationStatus(Long popId, Boolean status) {
        return popupMapper.updateModerationStatus(popId, status) > 0;
    }

    /**
     * 팝업스토어 상태 변경
     */
    @Override
    @Transactional
    public boolean updatePopupStatus(Long popId, String status) {
        return popupMapper.updatePopupStatus(popId, status) > 0;
    }

    /**
     * 팝업스토어 삭제 (soft delete)
     */
    @Override
    @Transactional
    public boolean deletePopup(Long popId) {
        return popupMapper.deletePopup(popId) > 0;
    }
}