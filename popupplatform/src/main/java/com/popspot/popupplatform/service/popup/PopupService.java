package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.domain.reservation.PopupReservation;
import com.popspot.popupplatform.dto.global.JwtUserDto;
import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupSortOption;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import com.popspot.popupplatform.dto.popup.request.PopupCreateRequest;
import com.popspot.popupplatform.dto.popup.request.PopupListRequest;
import com.popspot.popupplatform.dto.popup.response.PopupDetailResponse;
import com.popspot.popupplatform.dto.popup.response.PopupListItemResponse;
import com.popspot.popupplatform.dto.popup.response.PopupListResponse;
import com.popspot.popupplatform.dto.popup.response.PopupNearbyItemResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.exception.code.CommonErrorCode;
import com.popspot.popupplatform.global.exception.code.PopupErrorCode;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.global.geo.GeoCodingService;
import com.popspot.popupplatform.global.geo.GeoPoint;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.postgres.PopupGeoMapper;
import com.popspot.popupplatform.mapper.reservation.PopupReservationMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import com.popspot.popupplatform.mapper.user.UserWishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupMapper popupMapper;
    private final PopupReservationMapper popupReservationMapper;
    private final UserMapper userMapper;
    private final UserWishlistMapper userWishlistMapper;
    private final PopupAiSummaryService popupAiSummaryService;
    private final GeoCodingService geoCodingService;

    // Postgres (nearby 전용)
    private final PopupGeoMapper popupGeoMapper;

    /**
     * 팝업 스토어 등록
     */
    @Transactional
    public long registerPopup(PopupCreateRequest request, Long managerId) {

        // 0. 매니저 권한 체크
        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!"MANAGER".equals(user.getRole())) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        // 0. 날짜 유효성 검사 (시작일 > 종료일이면 에러)
        if (request.getPopEndDate().isBefore(request.getPopStartDate())) {
            throw new CustomException(PopupErrorCode.INVALID_DATE_RANGE);
        }

        // 1. 가격, 현황 타입 계산
        Integer price = request.getPopPrice();
        PopupPriceType priceType = (price == null || price == 0)
                ? PopupPriceType.FREE : PopupPriceType.PAID;

        PopupStatus initialStatus = LocalDateTime.now().isAfter(request.getPopStartDate())
                ? PopupStatus.ONGOING
                : PopupStatus.UPCOMING;

        // 2. 주소 -> 좌표 계산
        log.info("[PopupRegister] 지오코딩 시도 - address={}", request.getPopLocation());

        GeoPoint geoPoint = geoCodingService.findCoordinates(request.getPopLocation())
                .orElse(null);

        if (geoPoint == null) {
            log.warn("[PopupRegister] 지오코딩 실패 - address={}, geoPoint=null", request.getPopLocation());
        } else {
            log.info("[PopupRegister] 지오코딩 성공 - address={}, lat={}, lng={}",
                    request.getPopLocation(), geoPoint.getLatitude(), geoPoint.getLongitude());
        }

        Double latitude = (geoPoint != null) ? geoPoint.getLatitude() : null;
        Double longitude = (geoPoint != null) ? geoPoint.getLongitude() : null;

        // 3. DTO -> Entity 변환
        PopupStore popupStore = PopupStore.builder()
                .popOwnerId(managerId)
                .popName(request.getPopName())
                .popDescription(request.getPopDescription())
                .popThumbnail(request.getPopThumbnail())
                .popLocation(request.getPopLocation())
                .popLatitude(latitude)
                .popLongitude(longitude)
                .popStartDate(request.getPopStartDate())
                .popEndDate(request.getPopEndDate())
                .popIsReservation(false)
                .popPriceType(priceType)
                .popPrice(price)
                .popStatus(initialStatus)
                .popInstaUrl(request.getPopInstaUrl())
                .popAiSummary(null)
                .build();

        // 4. DB 저장 (MySQL)
        popupMapper.insertPopup(popupStore);
        Long newPopupId = popupStore.getPopId();

        // 4-1. 커밋 이후 Postgres에 upsert (nearby 분리 핵심)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void suspend() {}
            @Override public void resume() {}
            @Override public void flush() {}
            @Override public void beforeCommit(boolean readOnly) {}
            @Override public void beforeCompletion() {}

            @Override
            public void afterCommit() {
                try {
                    popupGeoMapper.upsertPopupGeo(popupStore);
                    log.info("[PopupGeoSync] postgres upsert ok. popId={}", newPopupId);
                } catch (Exception e) {
                    // MySQL 등록은 성공했는데 Postgres 동기화만 실패할 수 있으니, 실패는 로깅만
                    log.warn("[PopupGeoSync] postgres upsert failed. popId={}", newPopupId, e);
                }
            }

            @Override public void afterCompletion(int status) {}
        });

        log.info("팝업 저장 완료: id={}, title={}", newPopupId, popupStore.getPopName());

        // 5. 해시태그 저장
        if (request.getHashtags() != null) {
            request.getHashtags().forEach(rawTag -> {
                String tagName = normalizeTag(rawTag);
                if (tagName != null && !tagName.isEmpty()) {
                    popupMapper.insertHashtag(tagName);
                    Long hashId = popupMapper.findHashtagIdByName(tagName)
                            .orElseThrow(() -> new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR));
                    popupMapper.insertPopupHashtag(newPopupId, hashId);
                }
            });
        }

        // 6. 상세 이미지 URL 저장
        if (request.getPopImages() != null) {
            for (int i = 0; i < request.getPopImages().size(); i++) {
                String imageUrl = request.getPopImages().get(i);
                if (imageUrl != null && !imageUrl.isBlank()) {
                    popupMapper.insertPopupImage(newPopupId, imageUrl, i + 1);
                }
            }
        }

        // 7. AI 요약은 트랜잭션 이후 비동기로 생성 + 업데이트
        popupAiSummaryService.generateAndUpdateSummaryAsync(
                newPopupId,
                request.getPopName(),
                request.getPopDescription(),
                request.getHashtags()
        );

        return newPopupId;
    }

    private String normalizeTag(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        return trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;
    }

    /**
     * 팝업 스토어 목록 (String Cursor 적용 버전)
     */
    @Transactional(readOnly = true)
    public PopupListResponse getPopupList(PopupListRequest request, Long userId) {

        int size = request.getSafeSize();
        String cursor = request.getCursor();

        Long cursorId = null;
        LocalDateTime cursorEndDate = null;
        Long cursorViewCount = null;
        Integer cursorStatusGroup = null;

        PopupSortOption sortOption = request.getSafeSort();

        if (cursor != null && !cursor.isBlank()) {
            try {
                String[] parts = cursor.split("_");

                if (sortOption == PopupSortOption.DEADLINE) {
                    cursorEndDate = LocalDateTime.parse(parts[0]);
                    cursorId = Long.parseLong(parts[1]);

                } else if (sortOption == PopupSortOption.VIEW) {
                    if (parts.length == 3) {
                        cursorStatusGroup = Integer.parseInt(parts[0]);
                        cursorViewCount = Long.parseLong(parts[1]);
                        cursorId = Long.parseLong(parts[2]);
                    } else if (parts.length == 2) {
                        cursorViewCount = Long.parseLong(parts[0]);
                        cursorId = Long.parseLong(parts[1]);
                    }

                } else if (sortOption == PopupSortOption.POPULAR) {
                    if (parts.length == 3) {
                        cursorStatusGroup = Integer.parseInt(parts[0]);
                        cursorViewCount = Long.parseLong(parts[1]); // popularityScore
                        cursorId = Long.parseLong(parts[2]);
                    } else if (parts.length == 2) {
                        cursorViewCount = Long.parseLong(parts[0]);
                        cursorId = Long.parseLong(parts[1]);
                    }

                } else {
                    cursorId = Long.parseLong(parts[0]);
                }
            } catch (Exception e) {
                cursorId = null;
                cursorViewCount = null;
                cursorStatusGroup = null;
                log.warn("Invalid cursor format: {}", cursor);
            }
        }

        LocalDate safeStartDate = request.getSafeStartDate();
        LocalDate safeEndDate = request.getSafeEndDate();
        Integer safeMinPrice = request.getSafeMinPrice();
        Integer safeMaxPrice = request.getSafeMaxPrice();

        String sortStr = (sortOption != null) ? sortOption.name() : null;
        List<String> keywords = request.getSafeKeywords();

        List<PopupStore> popupStores = popupMapper.selectPopupList(
                cursorId,
                cursorEndDate,
                cursorViewCount,
                cursorStatusGroup,
                size + 1,
                keywords,
                request.getRegions(),
                safeStartDate,
                safeEndDate,
                request.getStatus(),
                safeMinPrice,
                safeMaxPrice,
                sortStr
        );

        boolean hasNext = false;
        String nextCursor = null;

        if (popupStores.size() > size) {
            hasNext = true;
            popupStores.remove(size);

            PopupStore lastItem = popupStores.get(popupStores.size() - 1);

            if (sortOption == PopupSortOption.DEADLINE) {
                nextCursor = lastItem.getPopEndDate().toString() + "_" + lastItem.getPopId();

            } else if (sortOption == PopupSortOption.VIEW) {
                LocalDateTime now = LocalDateTime.now();
                int statusGroup = 1;
                if (lastItem.getPopEndDate() != null && !lastItem.getPopEndDate().isBefore(now)) {
                    statusGroup = 0;
                }

                long viewCount = lastItem.getPopViewCount() != null ? lastItem.getPopViewCount() : 0L;
                nextCursor = statusGroup + "_" + viewCount + "_" + lastItem.getPopId();

            } else if (sortOption == PopupSortOption.POPULAR) {
                int statusGroup = (lastItem.getPopStatus() == PopupStatus.ENDED) ? 1 : 0;
                long popularityScore = lastItem.getPopPopularityScore() != null ? lastItem.getPopPopularityScore() : 0L;
                nextCursor = statusGroup + "_" + popularityScore + "_" + lastItem.getPopId();

            } else {
                nextCursor = String.valueOf(lastItem.getPopId());
            }
        }

        Set<Long> likedIdSet = null;

        if (userId != null && !popupStores.isEmpty()) {
            List<Long> popupIds = popupStores.stream()
                    .map(PopupStore::getPopId)
                    .collect(Collectors.toList());
            List<Long> likedPopupIds = userWishlistMapper.findLikedPopupIds(userId, popupIds);
            likedIdSet = new HashSet<>(likedPopupIds);
        }

        Long finalUserId = userId;
        Set<Long> finalLikedIdSet = likedIdSet;

        List<PopupListItemResponse> content = popupStores.stream()
                .map(store -> toPopupListItemResponse(store, finalUserId, finalLikedIdSet))
                .collect(Collectors.toList());

        return PopupListResponse.builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private PopupListItemResponse toPopupListItemResponse(PopupStore store,
                                                          Long userId,
                                                          Set<Long> likedIdSet) {

        Boolean isLiked = null;

        if (userId != null) {
            boolean liked = (likedIdSet != null) && likedIdSet.contains(store.getPopId());
            isLiked = liked;
        }

        return PopupListItemResponse.builder()
                .popId(store.getPopId())
                .popName(store.getPopName())
                .popThumbnail(store.getPopThumbnail())
                .popLocation(store.getPopLocation())
                .popStartDate(store.getPopStartDate())
                .popEndDate(store.getPopEndDate())
                .popStatus(store.getPopStatus())
                .popPriceType(store.getPopPriceType())
                .popPrice(store.getPopPrice())
                .popViewCount(store.getPopViewCount())
                .hashtags(Collections.emptyList())
                .isLiked(isLiked)
                .build();
    }

    /**
     * 찜 토글
     */
    @Transactional
    public boolean toggleWishlist(Long popId, Long userId) {

        if (!popupMapper.existsById(popId)) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }

        Boolean exists = userWishlistMapper.existsByUserIdAndPopId(userId, popId);

        if (Boolean.TRUE.equals(exists)) {
            userWishlistMapper.deleteWishlist(userId, popId);
            return false;
        } else {
            userWishlistMapper.insertWishlist(userId, popId);
            return true;
        }
    }

    /**
     * 팝업 상세 조회
     */
    @Transactional
    public PopupDetailResponse getPopupDetail(Long popupId, Long userId) {

        if (userId == null) {
            popupMapper.updateViewCount(popupId);
        } else {
            boolean viewedRecently = popupMapper.existsViewHistoryRecent(popupId, userId);
            if (!viewedRecently) {
                popupMapper.insertViewHistory(popupId, userId);
                popupMapper.updateViewCount(popupId);
            }
        }

        PopupStore popup = popupMapper.selectPopupDetail(popupId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        List<String> images = popupMapper.selectPopupImages(popupId);
        List<String> hashtags = popupMapper.selectPopupHashtags(popupId);

        Boolean isLiked = null;
        if (userId != null) {
            Boolean exists = userWishlistMapper.existsByUserIdAndPopId(userId, popupId);
            isLiked = Boolean.TRUE.equals(exists);
        }

        String reservationStatus = "NONE";
        LocalDateTime reservationStartTime = null;
        LocalDateTime reservationEndTime = null;

        if (Boolean.TRUE.equals(popup.getPopIsReservation())) {
            reservationStartTime = popupMapper.selectReservationStartTime(popupId);
            reservationEndTime = popupMapper.selectReservationEndTime(popupId);

            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(popup.getPopEndDate())) {
                reservationStatus = "CLOSED";
            } else if (reservationStartTime != null && now.isBefore(reservationStartTime)) {
                reservationStatus = "UPCOMING";
            } else {
                reservationStatus = "OPEN";
            }
        }

        PopupReservation popupReservation = popupReservationMapper.findByPopId(popupId);
        int maxPeoplePerReservation = 0;
        if (popupReservation != null) {
            maxPeoplePerReservation = popupReservation.getPrMaxUserCnt();
        }

        return PopupDetailResponse.builder()
                .popId(popup.getPopId())
                .popOwnerId(popup.getPopOwnerId())
                .popName(popup.getPopName())
                .popDescription(popup.getPopDescription())
                .popThumbnail(popup.getPopThumbnail())
                .popLocation(popup.getPopLocation())
                .popStartDate(popup.getPopStartDate())
                .popEndDate(popup.getPopEndDate())
                .popInstaUrl(popup.getPopInstaUrl())
                .popIsReservation(popup.getPopIsReservation())
                .maxPeoplePerReservation(maxPeoplePerReservation)
                .popPriceType(popup.getPopPriceType())
                .popPrice(popup.getPopPrice())
                .popStatus(popup.getPopStatus())
                .popViewCount(popup.getPopViewCount())
                .popAiSummary(popup.getPopAiSummary())
                .images(images)
                .hashtags(hashtags)
                .isLiked(isLiked)
                .reservationStartTime(reservationStartTime)
                .reservationEndTime(reservationEndTime)
                .reservationStatus(reservationStatus)
                .build();
    }

    /**
     * 내 주변 팝업 조회 (Postgres/PostGIS)
     */
    @Transactional(transactionManager = "postgresTxManager", readOnly = true)
    public List<PopupNearbyItemResponse> getNearbyPopups(
            Double latitude,
            Double longitude,
            Double radiusKm,
            Integer size,
            Long userId
    ) {
        if (latitude == null || longitude == null) {
            throw new CustomException(CommonErrorCode.INVALID_REQUEST);
        }

        double effectiveRadiusKm =
                (radiusKm == null || radiusKm <= 0) ? 3.0 : radiusKm;

        int defaultLimit = 30;
        int maxLimit = 500000;

        int limit = (size == null || size <= 0)
                ? defaultLimit
                : Math.min(size, maxLimit);

        long start = System.currentTimeMillis();

        log.info("[PopupNearby] 요청 - lat={}, lng={}, radiusKm={}, limit={}",
                latitude, longitude, effectiveRadiusKm, limit);

        List<PopupNearbyItemResponse> items =
                popupGeoMapper.selectNearbyPopups(latitude, longitude, effectiveRadiusKm, limit);

        long elapsed = System.currentTimeMillis() - start;

        log.info("[PopupNearby] 결과 개수 = {}, DB 소요 시간 = {}ms",
                items.size(), elapsed);

        if (userId == null || items.isEmpty()) {
            return items;
        }

        List<Long> popupIds = items.stream()
                .map(PopupNearbyItemResponse::getPopId)
                .collect(Collectors.toList());

        // MySQL에서 찜 여부 조회 (읽기만이라 트랜잭션 없어도 OK)
        List<Long> likedPopupIds = userWishlistMapper.findLikedPopupIds(userId, popupIds);
        Set<Long> likedIdSet = new HashSet<>(likedPopupIds);

        items.forEach(item -> item.setIsLiked(likedIdSet.contains(item.getPopId())));

        return items;
    }
}
