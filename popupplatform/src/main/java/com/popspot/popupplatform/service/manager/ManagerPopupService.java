package com.popspot.popupplatform.service.manager;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.global.JwtUserDto;
import com.popspot.popupplatform.dto.popup.request.ManagerPopupUpdateRequest;
import com.popspot.popupplatform.dto.popup.response.ManagerPopupDetailResponse;
import com.popspot.popupplatform.dto.user.response.ManagerReservationResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.exception.code.CommonErrorCode;
import com.popspot.popupplatform.global.exception.code.PopupErrorCode;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.manager.ManagerPopupMapper;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import com.popspot.popupplatform.service.popup.PopupAiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerPopupService {

    private final ManagerPopupMapper managerPopupMapper;
    private final UserMapper userMapper;

    private final PopupMapper popupMapper;
    private final PopupAiSummaryService popupAiSummaryService;


    /**
     * 1. 팝업 상세 정보 조회
     */
    public ManagerPopupDetailResponse getPopupDetail(Long managerId, Long popId) {
        return managerPopupMapper.selectPopupDetail(popId, managerId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));
    }

    /**
     * 2. 예약자 목록 조회
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

    /**
     * 3. 팝업 전체 정보 수정 (기본정보 + 이미지 + 해시태그)
     */
    @Transactional
    public void updatePopupBasicInfo(Long managerId, Long popId, ManagerPopupUpdateRequest request) {

        //매니저 권한 및 날짜/빈값 검증 로직
        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        if (!"MANAGER".equals(user.getRole())) throw new CustomException(AuthErrorCode.ACCESS_DENIED);

        ManagerPopupDetailResponse currentInfo = managerPopupMapper.selectPopupDetail(popId, managerId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        LocalDateTime newStart = (request.getPopStartDate() != null) ? request.getPopStartDate() : currentInfo.getPopStartDate();
        LocalDateTime newEnd   = (request.getPopEndDate() != null) ? request.getPopEndDate() : currentInfo.getPopEndDate();
        if (newEnd.isBefore(newStart)) throw new CustomException(PopupErrorCode.INVALID_DATE_RANGE);

        if (request.getPopName() != null && request.getPopName().isBlank()) {
            throw new CustomException(PopupErrorCode.TITLE_REQUIRED);
        }

        if (request.getPopDescription() != null && request.getPopDescription().isBlank()) {
            throw new CustomException(PopupErrorCode.DESCRIPTION_REQUIRED);
        }

        if (request.getPopThumbnail() != null && request.getPopThumbnail().isBlank()) {
            throw new CustomException(PopupErrorCode.THUMBNAIL_REQUIRED);
        }

        if (request.getPopLocation() != null && request.getPopLocation().isBlank()) {
            throw new CustomException(PopupErrorCode.LOCATION_REQUIRED);
        }

        if (request.getPopPrice() != null && request.getPopPrice() < 0) {
            throw new CustomException(PopupErrorCode.INVALID_PRICE);
        }

        //기본 정보 업데이트
        int updatedRows = managerPopupMapper.updatePopup(popId, managerId, request);
        if (updatedRows == 0) throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);

        //이미지 수정 (Null이면 건드리지 않음 / 빈 리스트면 모두 삭제)
        if (request.getPopImages() != null) {
            // 1. 기존 이미지 삭제
            managerPopupMapper.deletePopupImages(popId);

            // 2. 새 이미지 등록
            for (int i = 0; i < request.getPopImages().size(); i++) {
                String imageUrl = request.getPopImages().get(i);
                if (imageUrl != null && !imageUrl.isBlank()) {
                    popupMapper.insertPopupImage(popId, imageUrl, i + 1);
                }
            }
        }

        //해시태그 수정
        if (request.getHashtags() != null) {
            // 1. 기존 해시태그 연결 삭제
            managerPopupMapper.deletePopupHashtags(popId);

            // 2. 새 해시태그 등록
            request.getHashtags().forEach(rawTag -> {
                String tagName = normalizeTag(rawTag);
                if (tagName != null && !tagName.isEmpty()) {
                    popupMapper.insertHashtag(tagName);
                    Long hashId = popupMapper.findHashtagIdByName(tagName)
                            .orElseThrow(() -> new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR));
                    popupMapper.insertPopupHashtag(popId, hashId);
                }
            });
        }

        //AI 요약 갱신
        String targetName = (request.getPopName() != null) ? request.getPopName() : currentInfo.getPopName();
        String targetDesc = (request.getPopDescription() != null) ? request.getPopDescription() : currentInfo.getPopDescription();
        List<String> targetTags = (request.getHashtags() != null) ? request.getHashtags() : List.of();

        popupAiSummaryService.generateAndUpdateSummaryAsync(popId, targetName, targetDesc, targetTags);
    }

    //태그 정규화 메서드
    private String normalizeTag(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        return trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;
    }

    /**
     * 4. 팝업 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePopup(Long managerId, Long popId) {
        //매니저 권한 체크
        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!"MANAGER".equals(user.getRole())) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        //삭제 실행 (Soft Delete)
        int updatedRows = managerPopupMapper.deletePopup(popId, managerId);

        if (updatedRows == 0) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }
    }
}