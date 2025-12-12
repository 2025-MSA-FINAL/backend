package com.popspot.popupplatform.service.manager;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.response.PopupListItemResponse;
import com.popspot.popupplatform.dto.user.LoginUserDto;
import com.popspot.popupplatform.dto.user.request.ChangePasswordRequest;
import com.popspot.popupplatform.dto.user.request.UpdateEmailRequest;
import com.popspot.popupplatform.dto.user.request.UpdatePhoneRequest;
import com.popspot.popupplatform.dto.user.response.ManagerProfileResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.manager.ManagerPageMapper;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerPageService {

    private final ManagerPageMapper managerPageMapper;
    private final UserMapper userMapper; // 기존 UserMapper 재활용!
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. 매니저 프로필 조회
     */
    public ManagerProfileResponse getMyProfile(Long userId) {
        return managerPageMapper.findProfileByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * 2. 내가 등록한 팝업 목록 조회
     */
    public PageDTO<PopupListItemResponse> getMyPopups(
            Long userId,
            PageRequestDTO pageRequest,
            String status,
            String moderation
    ) {
        int page = Math.max(pageRequest.getPage(), 0);
        int size = Math.max(pageRequest.getSize(), 1);
        int offset = page * size;

        String rawSort = pageRequest.getSortBy();
        if (rawSort == null) {
            rawSort = "CREATED";
        }

        String sort = switch (rawSort) {
            case "DEADLINE", "VIEW", "POPULAR", "CREATED" -> rawSort;
            default -> "CREATED";
        };

        String effectiveStatus = (status == null || status.isEmpty()) ? "ALL" : status;
        String effectiveModeration = (moderation == null || moderation.isEmpty()) ? "ALL" : moderation;

        List<PopupListItemResponse> content = managerPageMapper.findMyPopups(
                userId, offset, size, effectiveStatus, effectiveModeration, sort
        );
        long total = managerPageMapper.countMyPopups(userId, effectiveStatus, effectiveModeration);

        return new PageDTO<>(content, page, size, total);
    }



    /**
     * 3. 이메일 수정 (UserMapper 재활용)
     */
    @Transactional
    public void updateEmail(Long userId, UpdateEmailRequest request) {
        // 중복 체크
        if (userMapper.countByEmail(request.getEmail()) > 0) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }
        userMapper.updateEmail(userId, request.getEmail());
    }

    /**
     * 4. 전화번호 수정 (UserMapper 재활용)
     */
    @Transactional
    public void updatePhone(Long userId, UpdatePhoneRequest request) {
        // 중복 체크
        if (userMapper.countByPhone(request.getPhone()) > 0) {
            throw new CustomException(UserErrorCode.DUPLICATE_PHONE);
        }
        userMapper.updatePhone(userId, request.getPhone());
    }

    /**
     * 5. 비밀번호 변경 (UserMapper 재활용)
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 현재 비번 확인을 위해 정보 조회
        LoginUserDto user = userMapper.findGeneralUserByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 일치 여부 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(UserErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 암호화 후 업데이트
        String encodedNewPwd = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(userId, encodedNewPwd);
    }
}