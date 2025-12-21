package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.admin.PopupStoreListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.admin.AdminPopupMapper;
import com.popspot.popupplatform.service.chat.ai.AiChatDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPopupServiceImpl implements AdminPopupService {

    private final AdminPopupMapper popupMapper;
    private final AiChatDocumentService aiChatDocumentService; //웹사이트 정보 ai 주입(팝업승인시 주입)
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
        int updated = popupMapper.updateModerationStatus(popId, status);
        if (updated == 0) return false;

        // 승인 / 반려 공통 처리
        if (Boolean.TRUE.equals(status)) {
            // 승인 → 기존 AI 문서 삭제 후 재생성
            PopupStore popup = popupMapper.findPopupEntityById(popId);

            aiChatDocumentService.deleteByPopupId(popId);
            savePopupAsAiDocument(popup);

        } else {
            // 반려 → AI 문서 제거
            aiChatDocumentService.deleteByPopupId(popId);
        }

        return true;
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
        boolean deleted = popupMapper.deletePopup(popId) > 0;
        if (deleted) {
            aiChatDocumentService.deleteByPopupId(popId);
        }
        return deleted;
    }

    /**
     * 전체 통계 조회 (필터 무관)
     */
    @Override
    public Map<String, Object> getPopupStats() {
        return popupMapper.getPopupStats();
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
                "status", popup.getPopStatus().name(),
                "priceType", popup.getPopPriceType().name()
        );

        aiChatDocumentService.save(content, metadata);
    }
}