package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.global.JwtUserDto;
import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupSortOption;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import com.popspot.popupplatform.dto.popup.request.PopupCreateRequest;
import com.popspot.popupplatform.dto.popup.request.PopupListRequest;
import com.popspot.popupplatform.dto.popup.response.PopupDetailResponse;
import com.popspot.popupplatform.dto.popup.response.PopupListItemResponse;
import com.popspot.popupplatform.dto.popup.response.PopupListResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.exception.code.CommonErrorCode;
import com.popspot.popupplatform.global.exception.code.PopupErrorCode;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
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
    private final UserMapper userMapper;
    private final UserWishlistMapper userWishlistMapper;
    private final PopupAiSummaryService popupAiSummaryService;

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

        // 2. DTO -> Entity 변환
        //    👉 AI 요약은 비동기로 따로 갱신하므로 여기선 null로 저장
        PopupStore popupStore = PopupStore.builder()
                .popOwnerId(managerId)
                .popName(request.getPopName())
                .popDescription(request.getPopDescription())
                .popThumbnail(request.getPopThumbnail())
                .popLocation(request.getPopLocation())
                .popStartDate(request.getPopStartDate())
                .popEndDate(request.getPopEndDate())
                .popIsReservation(request.getPopIsReservation())
                .popPriceType(priceType)
                .popPrice(price)
                .popStatus(initialStatus)
                .popInstaUrl(request.getPopInstaUrl())
                .popAiSummary(null) // ✅ 처음에는 비워둔다
                .build();

        // 3. DB 저장
        popupMapper.insertPopup(popupStore);
        Long newPopupId = popupStore.getPopId();

        log.info("팝업 저장 완료: id={}, title={}", newPopupId, popupStore.getPopName());

        // 4. 해시태그 저장
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

        // 5. 상세 이미지 URL 저장
        if (request.getPopImages() != null) {
            for (int i = 0; i < request.getPopImages().size(); i++) {
                String imageUrl = request.getPopImages().get(i);
                if (imageUrl != null && !imageUrl.isBlank()) {
                    // 순서는 1부터 시작 (i + 1)
                    popupMapper.insertPopupImage(newPopupId, imageUrl, i + 1);
                }
            }
        }

        // 6. 트랜잭션은 이후 AI 요약은 별도 쓰레드에서 비동기로 생성 + DB 업데이트
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

        // 변수명 충돌 방지를 위해 sortOption으로 변경
        PopupSortOption sortOption = request.getSafeSort();

        if (cursor != null && !cursor.isBlank()) {
            try {
                String[] parts = cursor.split("_");

                // 정렬 타입에 따라 파싱 전략 다름
                if (sortOption == PopupSortOption.DEADLINE) {
                    // 형식: "2025-12-31T00:00:00_105"
                    cursorEndDate = LocalDateTime.parse(parts[0]);
                    cursorId = Long.parseLong(parts[1]);
                } else if (sortOption == PopupSortOption.VIEW || sortOption == PopupSortOption.POPULAR) {
                    // 형식: "500_105"
                    cursorViewCount = Long.parseLong(parts[0]);
                    cursorId = Long.parseLong(parts[1]);
                } else {
                    // 형식: "105" (기본 최신순)
                    cursorId = Long.parseLong(parts[0]);
                }
            } catch (Exception e) {
                // 커서 포맷이 이상하면 0페이지(처음)부터 조회
                cursorId = null;
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

            // 제거되고 남은 "현재 페이지의 진짜 마지막 아이템"을 기준으로 커서를 생성해야 함
            PopupStore lastItem = popupStores.get(popupStores.size() - 1);

            // 정렬 기준에 따라 다음 커서 문자열 조합 (lastItem 사용!)
            if (sortOption == PopupSortOption.DEADLINE) {
                // 날짜 + "_" + ID
                nextCursor = lastItem.getPopEndDate().toString() + "_" + lastItem.getPopId();
            } else if (sortOption == PopupSortOption.VIEW || sortOption == PopupSortOption.POPULAR) {
                // 조회수 + "_" + ID
                nextCursor = lastItem.getPopViewCount() + "_" + lastItem.getPopId();
            } else {
                // ID만
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

        // 1. 조회수 증가 (삭제된 팝업이면 0 row 업데이트)
        popupMapper.updateViewCount(popupId);

        // 2. 기본 정보 조회 (소프트 삭제된 팝업 제외)
        PopupStore popup = popupMapper.selectPopupDetail(popupId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        // 3. 상세 이미지 & 해시태그 조회
        List<String> images   = popupMapper.selectPopupImages(popupId);
        List<String> hashtags = popupMapper.selectPopupHashtags(popupId);

        // 4. 로그인 유저 찜 여부 확인
        Boolean isLiked = null;     // 비로그인: null
        if (userId != null) {
            Boolean exists = userWishlistMapper.existsByUserIdAndPopId(userId, popupId);
            isLiked = Boolean.TRUE.equals(exists);
        }

        // 5. 예약 상태 계산
        // 기본값 NONE : 예약 개념이 없는 팝업
        String reservationStatus = "NONE";
        LocalDateTime reservationStartTime = null;

        if (Boolean.TRUE.equals(popup.getPopIsReservation())) {
            // 예약형 팝업인 경우에만 DB 조회
            reservationStartTime = popupMapper.selectReservationStartTime(popupId);

            LocalDateTime now = LocalDateTime.now();

            // 1) 팝업 자체가 종료된 경우 -> 예약도 마감
            if (now.isAfter(popup.getPopEndDate())) {
                reservationStatus = "CLOSED";
            }
            // 2) 예약 오픈 시간이 설정되어 있고, 아직 그 전인 경우 -> 오픈 예정
            else if (reservationStartTime != null && now.isBefore(reservationStartTime)) {
                reservationStatus = "UPCOMING";
            }
            // 3) 그 외 (오픈 시간 지남 or 시간 설정 안 함) -> 예약 가능
            else {
                reservationStatus = "OPEN";
            }
        }

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
                .popPriceType(popup.getPopPriceType())
                .popPrice(popup.getPopPrice())
                .popStatus(popup.getPopStatus())
                .popViewCount(popup.getPopViewCount())
                .popAiSummary(popup.getPopAiSummary())
                .images(images)
                .hashtags(hashtags)
                .isLiked(isLiked)
                .reservationStartTime(reservationStartTime) // 예약 없는 팝업이면 null
                .reservationStatus(reservationStatus)       // NONE / UPCOMING / OPEN / CLOSED
                .build();
    }
}