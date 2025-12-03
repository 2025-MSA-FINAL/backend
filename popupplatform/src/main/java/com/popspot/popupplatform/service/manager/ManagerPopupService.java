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
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerPopupService {

    private final ManagerPopupMapper managerPopupMapper;
    private final UserMapper userMapper;


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
     * 3. 팝업 기본 정보 수정
     */
    @Transactional
    public void updatePopupBasicInfo(Long managerId, Long popId, ManagerPopupUpdateRequest request) {

        //매니저 권한 체크
        JwtUserDto user = userMapper.findJwtUserByUserId(managerId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!"MANAGER".equals(user.getRole())) {
            throw new CustomException(AuthErrorCode.ACCESS_DENIED);
        }

        //기존 데이터 조회
        ManagerPopupDetailResponse currentInfo = managerPopupMapper.selectPopupDetail(popId, managerId)
                .orElseThrow(() -> new CustomException(PopupErrorCode.POPUP_NOT_FOUND));

        //날짜 유효성 검사
        LocalDateTime newStart = (request.getPopStartDate() != null)
                ? request.getPopStartDate()
                : currentInfo.getPopStartDate();

        LocalDateTime newEnd   = (request.getPopEndDate() != null)
                ? request.getPopEndDate()
                : currentInfo.getPopEndDate();

        if (newEnd.isBefore(newStart)) {
            throw new CustomException(PopupErrorCode.INVALID_DATE_RANGE); // "종료일이 시작일보다 빠를 수 없습니다"
        }

        //빈 문자열 검사
        if (request.getPopName() != null && request.getPopName().isBlank()) {
            throw new CustomException(CommonErrorCode.INVALID_REQUEST); // "잘못된 요청입니다"
        }
        if (request.getPopDescription() != null && request.getPopDescription().isBlank()) {
            throw new CustomException(CommonErrorCode.INVALID_REQUEST);
        }

        //업데이트 실행
        managerPopupMapper.updatePopup(popId, managerId, request);
    }
}