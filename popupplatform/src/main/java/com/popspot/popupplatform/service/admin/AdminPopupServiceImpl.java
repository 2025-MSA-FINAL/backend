package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminPopupDetailResponseDTO;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.response.ManagerPopupDetailResponse;  // ✅ import 추가
import com.popspot.popupplatform.mapper.admin.AdminPopupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPopupServiceImpl implements AdminPopupService {

    private final AdminPopupMapper adminPopupMapper;

    @Override
    public PageDTO<PopupStoreListDTO> getPopupList(
            PageRequestDTO pageRequest,
            String keyword,
            String status,
            String moderation,
            String deletedFilter) {

        List<PopupStoreListDTO> list = adminPopupMapper.findPopupList(
                pageRequest, keyword, status, moderation, deletedFilter
        );

        long total = adminPopupMapper.countPopupList(
                keyword, status, moderation, deletedFilter
        );

        return new PageDTO<>(
                list,
                pageRequest.getPage(),
                pageRequest.getSize(),
                total
        );
    }


    /**
     * 추가: 관리자용 팝업 상세 조회 (매니저 뷰)
     */
    @Override
    public AdminPopupDetailResponseDTO getPopupDetailForAdmin(Long popId) {
        log.info("관리자용 팝업 상세 조회: popId={}", popId);

        AdminPopupDetailResponseDTO popup = adminPopupMapper.findPopupDetailForAdmin(popId);

        if (popup == null) {
            log.warn("팝업을 찾을 수 없습니다. popId={}", popId);
            throw new IllegalArgumentException("팝업을 찾을 수 없습니다. (ID: " + popId + ")");
        }

        log.info("팝업 상세 조회 성공: {}", popup.getPopName());
        return popup;
    }

    @Override
    @Transactional
    public boolean updateModerationStatus(Long popId, Boolean status, String reason) {
        log.info("승인 상태 변경: popId={}, status={}, reason={}", popId, status, reason);

        int updated = adminPopupMapper.updateModerationStatus(popId, status);

        if (updated > 0 && reason != null) {
            // TODO: 승인 이력 저장 로직
            // adminPopupMapper.insertPopupModeration(...);
        }

        return updated > 0;
    }

    @Override
    @Transactional
    public boolean updatePopupStatus(Long popId, String status) {
        log.info("팝업 상태 변경: popId={}, status={}", popId, status);
        return adminPopupMapper.updatePopupStatus(popId, status) > 0;
    }

    @Override
    @Transactional
    public boolean deletePopup(Long popId, String reason) {
        log.info("팝업 삭제: popId={}, reason={}", popId, reason);

        int deleted = adminPopupMapper.deletePopup(popId);

        if (deleted > 0 && reason != null) {
            // TODO: 삭제 이력 저장 로직
        }

        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean restorePopup(Long popId) {
        log.info("팝업 복구: popId={}", popId);
        return adminPopupMapper.restorePopup(popId) > 0;
    }

    @Override
    public Map<String, Object> getPopupStats() {
        return adminPopupMapper.getPopupStats();
    }
}