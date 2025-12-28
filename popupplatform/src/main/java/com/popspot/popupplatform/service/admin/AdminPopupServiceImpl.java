package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.domain.admin.PopupModerationStatus;
import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.admin.AdminPopupDetailResponseDTO;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.response.ManagerPopupDetailResponse;  // ✅ import 추가
import com.popspot.popupplatform.mapper.admin.AdminPopupMapper;
import com.popspot.popupplatform.service.chat.ai.AiChatDocumentService;
import com.popspot.popupplatform.service.popup.PopupGeoSyncService;
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

    private final AiChatDocumentService aiChatDocumentService;


    // Postgres Geo 동기화 서비스
    private final PopupGeoSyncService popupGeoSyncService;


    private String moderationMessage(PopupModerationStatus status) {
        return switch (status) {
            case APPROVED -> "관리자에 의해 승인됨";
            case REJECTED -> "관리자에 의해 반려됨";
            case PENDING  -> "관리자에 의해 대기 처리됨";
            default -> null;
        };
    }


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


        if (updated == 0) return false;

        // 승인 / 반려 공통 처리
        if (Boolean.TRUE.equals(status)) {
            // 승인 → 기존 AI 문서 삭제 후 재생성
            PopupStore popup = adminPopupMapper.findPopupEntityById(popId);

            aiChatDocumentService.deleteByPopupId(popId);
            savePopupAsAiDocument(popup);

        } else {
            // 반려 → AI 문서 제거
            aiChatDocumentService.deleteByPopupId(popId);
        }

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

            adminPopupMapper.insertPopupModeration(
                    popId,
                    2L,
                    pmStatus.name(),
                    comment
            );

            log.info("팝업 승인 상태 이력 기록: popId={}, status={}, comment={}", popId, pmStatus, comment);






            popupGeoSyncService.syncPopup(popId);




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

    /* =====================================================
    팝업 → AI 문서 변환
     ===================================================== */

    private void savePopupAsAiDocument(PopupStore popup) {

        if (popup == null) return;

        String content = """
            팝업스토어 이름: %s
        
            요약:
            %s
        
            운영 기간:
            %s ~ %s
        
            장소:
            %s
        
            가격:
            %s (%s원)
        
            상태:
            %s
        """.formatted(
                popup.getPopName(),
                popup.getPopAiSummary() != null
                        ? popup.getPopAiSummary()
                        : popup.getPopDescription(),
                popup.getPopStartDate(),
                popup.getPopEndDate(),
                popup.getPopLocation(),
                popup.getPopPriceType(),
                popup.getPopPrice(),
                popup.getPopStatus()
        );

        Map<String, Object> metadata = Map.of(
                "type", "popup",
                "popupId", popup.getPopId(),
                "name", popup.getPopName(),
                "status", popup.getPopStatus().name(),
                "priceType", popup.getPopPriceType().name()
        );

        aiChatDocumentService.save(content, metadata);
    }
}