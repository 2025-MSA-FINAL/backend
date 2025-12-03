package com.popspot.popupplatform.service.manager;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.response.ManagerPopupDetailResponse;
import com.popspot.popupplatform.dto.user.response.ManagerReservationResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.PopupErrorCode;
import com.popspot.popupplatform.mapper.manager.ManagerPopupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerPopupService {

    private final ManagerPopupMapper managerPopupMapper;

    /**
     * 1. 팝업 상세 정보 조회
     */
    public ManagerPopupDetailResponse getPopupDetail(Long managerId, Long popId) {
        return managerPopupMapper.selectPopupDetail(popId, managerId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));
    }

    /**
     * 2. 예약자 목록 조회 (페이지네이션)
     */
    public PageDTO<ManagerReservationResponse> getReservations(Long managerId, Long popId, PageRequestDTO pageRequest) {
        //내 팝업인지 검증
        if (managerPopupMapper.selectPopupDetail(popId, managerId).isEmpty()) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }

        int page = Math.max(pageRequest.getPage(), 0);
        int size = Math.max(pageRequest.getSize(), 1);
        int offset = page * size;

        List<ManagerReservationResponse> content = managerPopupMapper.selectReservations(
                popId, offset, size
        );

        long total = managerPopupMapper.countReservations(popId);

        return new PageDTO<>(content, page, size, total);
    }
}