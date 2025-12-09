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
import com.popspot.popupplatform.mapper.reservation.PopupReservationMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import com.popspot.popupplatform.mapper.user.UserWishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                ? PopupStatus.ONGOING  // 시작일 지났으면 '진행 중'
                : PopupStatus.UPCOMING; // 아니면 '오픈 예정'


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

        Double latitude  = (geoPoint != null) ? geoPoint.getLatitude()  : null;
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
                .popIsReservation(request.getPopIsReservation())
                .popPriceType(priceType)
                .popPrice(price)
                .popStatus(initialStatus)
                .popInstaUrl(request.getPopInstaUrl())
                .popAiSummary(null)
                .build();

        // 4. DB 저장
        popupMapper.insertPopup(popupStore);
        Long newPopupId = popupStore.getPopId();

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
                    // 순서는 1부터 시작 (i + 1)
                    popupMapper.insertPopupImage(newPopupId, imageUrl, i + 1);
                }
            }
        }

        // 7. AI 요약은 트랜잭션 이후 별도 쓰레드에서 비동기로 생성 + DB 업데이트
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

        // 1. size 계산
        int size = request.getSafeSize();

        // 2. 요청 커서 파싱 (String -> 값 분리)
        String cursor = request.getCursor();

        Long cursorId = null;
        LocalDateTime cursorEndDate = null;
        Long cursorViewCount = null;
        Integer cursorStatusGroup = null;

        PopupSortOption sortOption = request.getSafeSort();

        if (cursor != null && !cursor.isBlank()) {
            try {
                String[] parts = cursor.split("_");

                // 정렬 타입에 따라 파싱 전략 다름
                if (sortOption == PopupSortOption.DEADLINE) {
                    // 형식: "2025-12-31T00:00:00_105"
                    cursorEndDate = LocalDateTime.parse(parts[0]);
                    cursorId      = Long.parseLong(parts[1]);
                } else if (sortOption == PopupSortOption.VIEW) {
                    // 새 포맷: "statusGroup_viewCount_id"
                    // 예) "0_532_105"
                    if (parts.length == 3) {
                        cursorStatusGroup = Integer.parseInt(parts[0]); // 0: 진행/예정, 1: 종료
                        cursorViewCount   = Long.parseLong(parts[1]);
                        cursorId          = Long.parseLong(parts[2]);
                    } else if (parts.length == 2) {
                        // 혹시 이전 포맷("viewCount_id")가 남아 있을 경우 대비
                        cursorViewCount = Long.parseLong(parts[0]);
                        cursorId        = Long.parseLong(parts[1]);
                    }
                } else if (sortOption == PopupSortOption.POPULAR) {
                    // 새 포맷: "statusGroup_popularityScore_id"
                    // 예) "0_1234_105"
                    if (parts.length == 3) {
                        cursorStatusGroup = Integer.parseInt(parts[0]); // 0: ENDED 아님, 1: ENDED
                        cursorViewCount   = Long.parseLong(parts[1]);   // popularityScore
                        cursorId          = Long.parseLong(parts[2]);
                    } else if (parts.length == 2) {
                        // 이전 포맷("score_id")가 남아 있을 경우 대비
                        cursorViewCount = Long.parseLong(parts[0]);
                        cursorId        = Long.parseLong(parts[1]);
                    }
                } else {
                    // 형식: "105" (CREATED 등 기본 최신순)
                    cursorId = Long.parseLong(parts[0]);
                }
            } catch (Exception e) {
                // 커서 포맷이 이상하면 0페이지(처음)부터 조회
                cursorId = null;
                cursorViewCount = null;
                cursorStatusGroup = null;
                log.warn("Invalid cursor format: {}", cursor);
            }
        }

        // 3. 필터 값 정리
        LocalDate safeStartDate = request.getSafeStartDate();
        LocalDate safeEndDate   = request.getSafeEndDate();
        Integer safeMinPrice    = request.getSafeMinPrice();
        Integer safeMaxPrice    = request.getSafeMaxPrice();

        // 4. 정렬 옵션 (Enum -> String 변환)
        String sortStr = (sortOption != null) ? sortOption.name() : null;

        // 5. DB 조회
        List<PopupStore> popupStores = popupMapper.selectPopupList(
                cursorId,
                cursorEndDate,
                cursorViewCount,
                cursorStatusGroup,
                size + 1,
                request.getKeyword(),
                request.getRegions(),
                safeStartDate,
                safeEndDate,
                request.getStatus(),
                safeMinPrice,
                safeMaxPrice,
                sortStr
        );

        // 6. hasNext, nextCursor 생성 (Encoding: 값_ID 조합)
        boolean hasNext = false;
        String nextCursor = null;

        if (popupStores.size() > size) {
            hasNext = true;
            popupStores.remove(size);

            PopupStore lastItem = popupStores.get(popupStores.size() - 1);

            if (sortOption == PopupSortOption.DEADLINE) {
                // 날짜 + "_" + ID
                nextCursor = lastItem.getPopEndDate().toString() + "_" + lastItem.getPopId();

            } else if (sortOption == PopupSortOption.VIEW) {
                // statusGroup: 진행/예정(0) vs 종료(1)
                LocalDateTime now = LocalDateTime.now();
                int statusGroup = 1; // 기본은 종료
                if (lastItem.getPopEndDate() != null && !lastItem.getPopEndDate().isBefore(now)) {
                    // endDate >= now 면 진행/예정
                    statusGroup = 0;
                }

                long viewCount = lastItem.getPopViewCount() != null
                        ? lastItem.getPopViewCount()
                        : 0L;

                // "statusGroup_viewCount_id"
                nextCursor = statusGroup + "_" + viewCount + "_" + lastItem.getPopId();

            } else if (sortOption == PopupSortOption.POPULAR) {
                // statusGroup: ENDED(1) vs 나머지(0)
                int statusGroup = (lastItem.getPopStatus() == PopupStatus.ENDED) ? 1 : 0;

                // 인기 점수(popularityScore): Mapper에서 popularity_score AS popularity_score 로 넘어온 값
                long popularityScore = lastItem.getPopPopularityScore() != null
                        ? lastItem.getPopPopularityScore()
                        : 0L;

                // "statusGroup_popularityScore_id"
                nextCursor = statusGroup + "_" + popularityScore + "_" + lastItem.getPopId();

            } else {
                // CREATED 등: ID만
                nextCursor = String.valueOf(lastItem.getPopId());
            }
        }

        // 7. PopupStore -> PopupListItemResponse 변환 + isLiked 채우기
        List<PopupListItemResponse> content;
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

        content = popupStores.stream()
                .map(store -> toPopupListItemResponse(store, finalUserId, finalLikedIdSet))
                .collect(Collectors.toList());

        // 8. 응답
        return PopupListResponse.builder()
                .content(content)
                .nextCursor(nextCursor) // String 값 전달
                .hasNext(hasNext)
                .build();
    }

    /**
     * 팝업 한 개 -> 응답 DTO로 변환
     */
    private PopupListItemResponse toPopupListItemResponse(PopupStore store,
                                                          Long userId,
                                                          Set<Long> likedIdSet) {

        Boolean isLiked = null;  // 비로그인은 null 유지

        if (userId != null) {
            // 로그인 상태일 때만 true/false 세팅
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
                // TODO: 해시태그 조회/매핑
                .hashtags(Collections.emptyList())
                .isLiked(isLiked)
                .build();
    }

    /**
     * 찜 토글
     */
    @Transactional
    public boolean toggleWishlist(Long popId, Long userId) {

        // 존재하지 않는 팝업이면 에러 발생
        if (!popupMapper.existsById(popId)) {
            throw new CustomException(PopupErrorCode.POPUP_NOT_FOUND);
        }

        // 이미 찜했는지 확인
        Boolean exists = userWishlistMapper.existsByUserIdAndPopId(userId, popId);

        if (Boolean.TRUE.equals(exists)) {
            userWishlistMapper.deleteWishlist(userId, popId);
            return false;   // 찜 해제
        } else {
            userWishlistMapper.insertWishlist(userId, popId);
            return true;    // 찜
        }
    }

    /**
     * 팝업 상세 조회
     */
    @Transactional
    public PopupDetailResponse getPopupDetail(Long popupId, Long userId) {

        // 1. 조회수 증가 로직 (중복 방지)
        if (userId == null) {
            // 비로그인 유저: 그냥 조회수 증가
            popupMapper.updateViewCount(popupId);
        } else {
            // 로그인 유저: '최근(1시간)' 조회 기록이 없을 때만 증가 + 기록 저장
            boolean viewedRecently = popupMapper.existsViewHistoryRecent(popupId, userId);

            if (!viewedRecently) {
                // 1) 기록 저장 (POPUP_VIEWED)
                popupMapper.insertViewHistory(popupId, userId);
                // 2) 카운트 증가 (POPUPSTORE)
                popupMapper.updateViewCount(popupId);
            }
        }

        // 2. 기본 정보 조회 (소프트 삭제된 팝업 제외)
        PopupStore popup = popupMapper.selectPopupDetail(popupId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        // 3. 상세 이미지 & 해시태그 조회
        List<String> images   = popupMapper.selectPopupImages(popupId);
        List<String> hashtags = popupMapper.selectPopupHashtags(popupId);

        // 4. 로그인 유저 찜 여부 확인
        Boolean isLiked = null;
        if (userId != null) {
            Boolean exists = userWishlistMapper.existsByUserIdAndPopId(userId, popupId);
            isLiked = Boolean.TRUE.equals(exists);
        }

        // 5. 예약 상태 계산
        String reservationStatus = "NONE";
        LocalDateTime reservationStartTime = null;

        if (Boolean.TRUE.equals(popup.getPopIsReservation())) {
            reservationStartTime = popupMapper.selectReservationStartTime(popupId);
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

        // 6. DTO 조립 및 반환
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
                .maxPeoplePerReservation(popupReservation.getPrMaxUserCnt())
                .popPriceType(popup.getPopPriceType())
                .popPrice(popup.getPopPrice())
                .popStatus(popup.getPopStatus())
                .popViewCount(popup.getPopViewCount())
                .popAiSummary(popup.getPopAiSummary())
                .images(images)
                .hashtags(hashtags)
                .isLiked(isLiked)
                .reservationStartTime(reservationStartTime)
                .reservationStatus(reservationStatus)
                .build();
    }


    /**
     * 내 주변 팝업 조회
     */
    @Transactional(readOnly = true)
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
                (radiusKm == null || radiusKm <= 0) ? 3.0 : radiusKm;   // 기본 3km
        int limit =
                (size == null || size <= 0 || size > 100) ? 30 : size;  // 기본 30개, 최대 100개

        long start = System.currentTimeMillis(); //시간 측정 시작

        log.info("[PopupNearby] 요청 - lat={}, lng={}, radiusKm={}, limit={}",
                latitude, longitude, effectiveRadiusKm, limit);

        List<PopupNearbyItemResponse> items =
                popupMapper.selectNearbyPopups(latitude, longitude, effectiveRadiusKm, limit);

        long elapsed = System.currentTimeMillis() - start; //걸린 시간 계산

        log.info("[PopupNearby] 결과 개수 = {}, DB 소요 시간 = {}ms",
                items.size(), elapsed);

        //비로그인 or 결과 없음 → isLiked 안 채우고 그대로 리턴
        if (userId == null || items.isEmpty()) {
            return items;
        }

        //로그인 상태면 찜한 팝업 ID 목록 조회
        List<Long> popupIds = items.stream()
                .map(PopupNearbyItemResponse::getPopId)
                .collect(Collectors.toList());

        List<Long> likedPopupIds = userWishlistMapper.findLikedPopupIds(userId, popupIds);
        Set<Long> likedIdSet = new HashSet<>(likedPopupIds);

        //각 아이템에 isLiked 채워주기
        items.forEach(item ->
                item.setIsLiked(likedIdSet.contains(item.getPopId()))
        );

        return items;
    }




}