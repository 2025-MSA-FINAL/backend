package com.popspot.popupplatform.service.manager;

import com.popspot.popupplatform.domain.popup.PopupStore;
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
import com.popspot.popupplatform.global.geo.GeoCodingService;
import com.popspot.popupplatform.mapper.manager.ManagerPopupMapper;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.postgres.PopupGeoMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import com.popspot.popupplatform.service.popup.PopupAiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerPopupService {

    private final ManagerPopupMapper managerPopupMapper;
    private final GeoCodingService geoCodingService;
    private final UserMapper userMapper;

    private final PopupMapper popupMapper;
    private final PopupAiSummaryService popupAiSummaryService;

    private final PopupGeoMapper popupGeoMapper;

    private String format;

    /**
     * 트랜잭션 커밋 후 실행 유틸 (MySQL 롤백 시 Postgres만 반영되는 사고 방지)
     */
    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            // 혹시라도 트랜잭션 밖에서 호출되는 경우
            task.run();
        }
    }

    /**
     * 1. 팝업 상세 정보 조회
     */
    public ManagerPopupDetailResponse getPopupDetail(Long userId, Long popId) {
        ManagerPopupDetailResponse response = managerPopupMapper.selectPopupDetail(popId, userId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        List<String> images = popupMapper.selectPopupImages(popId);
        response.setPopImages(images);

        List<String> hashtags = popupMapper.selectPopupHashtags(popId);
        response.setHashtags(hashtags);

        return response;
    }

    /**
     * 2. 예약자 목록 조회
     */
    public PageDTO<ManagerReservationResponse> getReservations(Long managerId, Long popId, PageRequestDTO pageRequest) {
        if (managerPopupMapper.selectPopupDetail(popId, managerId).isEmpty()) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }

        int page = Math.max(pageRequest.getPage(), 0);
        int size = Math.max(pageRequest.getSize(), 1);
        int offset = page * size;

        List<ManagerReservationResponse> content = managerPopupMapper.selectReservations(popId, offset, size);
        long total = managerPopupMapper.countReservations(popId);

        return new PageDTO<>(content, page, size, total);
    }

    /**
     * 3. 팝업 전체 정보 수정 (기본정보 + 이미지 + 해시태그)
     */
    @Transactional
    public void updatePopupBasicInfo(Long managerId, Long popId, ManagerPopupUpdateRequest request) {

        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        if (!"MANAGER".equals(user.getRole())) throw new CustomException(AuthErrorCode.ACCESS_DENIED);

        ManagerPopupDetailResponse currentInfo = managerPopupMapper.selectPopupDetail(popId, managerId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        LocalDateTime newStart = (request.getPopStartDate() != null)
                ? request.getPopStartDate()
                : currentInfo.getPopStartDate();
        LocalDateTime newEnd = (request.getPopEndDate() != null)
                ? request.getPopEndDate()
                : currentInfo.getPopEndDate();
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

        //주소/좌표 처리 로직
        if (request.getPopLocation() != null) {
            boolean locationChanged = !request.getPopLocation().equals(currentInfo.getPopLocation());
            boolean coordinatesMissingInDb = currentInfo.getPopLatitude() == null || currentInfo.getPopLongitude() == null;
            boolean needGeocoding = locationChanged || coordinatesMissingInDb;

            if (needGeocoding) {
                log.info("[ManagerPopupUpdate] 주소/좌표 업데이트 필요 - popId={}, locationChanged={}, coordinatesMissing={}",
                        popId, locationChanged, coordinatesMissingInDb);

                log.info("[ManagerPopupUpdate] 지오코딩 시도 - address={}", request.getPopLocation());

                geoCodingService.findCoordinates(request.getPopLocation())
                        .ifPresentOrElse(
                                geoPoint -> {
                                    request.setPopLatitude(geoPoint.getLatitude());
                                    request.setPopLongitude(geoPoint.getLongitude());
                                    log.info("[ManagerPopupUpdate] 지오코딩 성공 - lat={}, lng={}",
                                            geoPoint.getLatitude(), geoPoint.getLongitude());
                                },
                                () -> {
                                    request.setPopLatitude(null);
                                    request.setPopLongitude(null);
                                    log.warn("[ManagerPopupUpdate] 지오코딩 실패 - address={}. 좌표를 NULL로 설정",
                                            request.getPopLocation());
                                }
                        );
            } else {
                log.info("[ManagerPopupUpdate] 주소 변경 없음 & 좌표 이미 존재 - 기존 좌표 유지 popId={}", popId);

                if (request.getPopLatitude() == null) request.setPopLatitude(currentInfo.getPopLatitude());
                if (request.getPopLongitude() == null) request.setPopLongitude(currentInfo.getPopLongitude());
            }
        }
        // ===== 주소 / 좌표 처리 끝 =====

        int updatedRows = managerPopupMapper.updatePopup(popId, managerId, request);
        if (updatedRows == 0) throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);

        // 이미지 수정
        if (request.getPopImages() != null) {
            managerPopupMapper.deletePopupImages(popId);
            for (int i = 0; i < request.getPopImages().size(); i++) {
                String imageUrl = request.getPopImages().get(i);
                if (imageUrl != null && !imageUrl.isBlank()) {
                    popupMapper.insertPopupImage(popId, imageUrl, i + 1);
                }
            }
        }

        // 해시태그 수정
        if (request.getHashtags() != null) {
            managerPopupMapper.deletePopupHashtags(popId);

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

        // AI 요약 갱신
        String targetName = (request.getPopName() != null) ? request.getPopName() : currentInfo.getPopName();
        String targetDesc = (request.getPopDescription() != null) ? request.getPopDescription() : currentInfo.getPopDescription();
        List<String> targetTags = (request.getHashtags() != null) ? request.getHashtags() : List.of();
        popupAiSummaryService.generateAndUpdateSummaryAsync(popId, targetName, targetDesc, targetTags);


        runAfterCommit(() -> {
            try {
                PopupStore latest = popupMapper.selectPopupDetail(popId).orElse(null);
                if (latest == null) {
                    log.warn("[PopupGeoSync] update 이후 MySQL popup 조회 실패. popId={}", popId);
                    return;
                }
                popupGeoMapper.upsertPopupGeo(latest);
                log.info("[PopupGeoSync] update 반영 완료. popId={}", popId);
            } catch (Exception e) {
                log.error("[PopupGeoSync] update 반영 실패. popId={}", popId, e);
            }
        });
    }

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
        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!"MANAGER".equals(user.getRole())) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        int updatedRows = managerPopupMapper.deletePopup(popId, managerId);
        if (updatedRows == 0) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }


        runAfterCommit(() -> {
            try {
                popupGeoMapper.softDeletePopupGeo(popId);
                log.info("[PopupGeoSync] delete(soft) 반영 완료. popId={}", popId);
            } catch (Exception e) {
                log.error("[PopupGeoSync] delete(soft) 반영 실패. popId={}", popId, e);
            }
        });
    }
}
