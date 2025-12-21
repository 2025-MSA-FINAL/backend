package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.domain.admin.DeletedFilter;
import com.popspot.popupplatform.domain.admin.PopupModerationStatus;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
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
@Transactional
public class AdminPopupServiceImpl implements AdminPopupService {

    private static final Long SYSTEM_ADMIN_ID = 2L;

    private final AdminPopupMapper popupMapper;
    // private final PopupModerationMapper moderationMapper;  // 이력 기록용 (있다면)


    private String moderationMessage(PopupModerationStatus status) {
        return switch (status) {
            case APPROVED -> "관리자에 의해 승인됨";
            case REJECTED -> "관리자에 의해 반려됨";
            case PENDING  -> "관리자에 의해 대기 처리됨";
            default -> null;
        };
    }

    /**
     *  팝업스토어 목록 조회 (deletedFilter 추가)
     */
    @Override
    @Transactional(readOnly = true)
    public PageDTO<PopupStoreListDTO> getPopupList(
            PageRequestDTO pageRequest,
            String keyword,
            String status,
            String moderation,
            String deletedFilterParam) {

        //  enum으로 안전하게 정제
        DeletedFilter deletedFilter = DeletedFilter.from(deletedFilterParam);

        List<PopupStoreListDTO> content = popupMapper.findPopupList(
                pageRequest,
                keyword,
                status,
                moderation,
                deletedFilter.getValue() // MyBatis에는 문자열만 전달
        );

        long total = popupMapper.countPopupList(
                keyword,
                status,
                moderation,
                deletedFilter.getValue()
        );

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
    @Transactional(readOnly = true)
    public PopupStoreListDTO getPopupDetail(Long popId) {
        return popupMapper.findPopupById(popId);
    }

    /**
     * 승인 상태 변경 (자유롭게 변경 가능)
     * - NULL(대기), true(승인), false(거절)을 자유롭게 변경
     */
    @Override
    public boolean updateModerationStatus(Long popId, Boolean status, String reason) {
        int updated = popupMapper.updateModerationStatus(popId, status);

        if (updated > 0) {
            PopupModerationStatus pmStatus =
                    status == null ? PopupModerationStatus.PENDING :
                            status ? PopupModerationStatus.APPROVED
                                    : PopupModerationStatus.REJECTED;

            // [수정 포인트] 우선순위: 직접 입력한 reason > 기본 메시지
            String comment;
            if (reason != null && !reason.trim().isEmpty()) {
                comment = reason; // 직접 입력한 사유가 있으면 최우선 적용
            } else {
                comment = moderationMessage(pmStatus); // 없으면 기본값 ("관리자에 의해 반려됨" 등)
            }

            popupMapper.insertPopupModeration(
                    popId,
                    SYSTEM_ADMIN_ID,
                    pmStatus.name(),
                    comment
            );

            log.info("팝업 승인 상태 이력 기록: popId={}, status={}, comment={}", popId, pmStatus, comment);
        }

        return updated > 0;
    }
    /**
     * 팝업스토어 상태 변경
     */
    @Override
    public boolean updatePopupStatus(Long popId, String status) {
        int updated = popupMapper.updatePopupStatus(popId, status);

        if (updated > 0) {
            log.info("팝업 상태 변경: popId={}, status={}", popId, status);
        }

        return updated > 0;
    }

    /**
     *  팝업스토어 삭제 (사유 포함)
     */
    @Override
    public boolean deletePopup(Long popId, String reason) {
        int deleted = popupMapper.deletePopup(popId);

        if (deleted > 0) {
            popupMapper.insertPopupModeration(
                    popId,
                    SYSTEM_ADMIN_ID,
                    PopupModerationStatus.DELETED.name(),
                    reason
            );
        }
        return deleted > 0;
    }



    /**
     *  팝업스토어 복구 (삭제 취소)
     */
    @Override
    public boolean restorePopup(Long popId) {
        int restored = popupMapper.restorePopup(popId);

        if (restored > 0) {
            popupMapper.insertPopupModeration(
                    popId,
                    1L, // adminId (단일 관리자면 하드코딩 OK)
                    PopupModerationStatus.RESTORED.name(),
                    "관리자에 의해 복구됨"
            );

            log.info("팝업 복구 이력 기록: popId={}", popId);
        }

        return restored > 0;
    }

    /**
     * 전체 통계 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPopupStats() {
        return popupMapper.getPopupStats();
    }
}