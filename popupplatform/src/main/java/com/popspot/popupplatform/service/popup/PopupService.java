package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.global.JwtUserDto;
import com.popspot.popupplatform.dto.popup.enums.PopupPriceType;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import com.popspot.popupplatform.dto.popup.request.PopupCreateRequest;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import com.popspot.popupplatform.global.exception.code.CommonErrorCode;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.popup.PopupMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupMapper popupMapper;
    private final UserMapper userMapper;
    // S3Service는 이제 여기서 안 씁니다! (컨트롤러나 별도 API에서 처리했으므로)

    /**
     * 팝업 스토어 등록 (순수 데이터 저장)
     * - 이미지는 URL 형태로 전달받음
     */
    @Transactional
    public long registerPopup(PopupCreateRequest request, Long managerId) {

        // 0. 매니저 권한 체크
        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!"MANAGER".equals(user.getRole())) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        // 1. 가격 타입 계산
        Integer price = request.getPopPrice();
        PopupPriceType priceType = (price == null || price == 0)
                ? PopupPriceType.FREE : PopupPriceType.PAID;

        // 2. DTO -> Entity 변환 (URL은 DTO에서 바로 꺼냄)
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
                .popStatus(PopupStatus.UPCOMING)
                .popInstaUrl(request.getPopInstaUrl())
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
                    //순서는 1부터 시작 (i + 1)
                    popupMapper.insertPopupImage(newPopupId, imageUrl, i + 1);
                }
            }
        }

        return newPopupId;
    }

    private String normalizeTag(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        return trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;
    }
}