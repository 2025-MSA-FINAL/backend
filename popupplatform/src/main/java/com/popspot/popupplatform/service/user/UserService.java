package com.popspot.popupplatform.service.user;

import com.popspot.popupplatform.dto.user.UserDto;
import com.popspot.popupplatform.dto.user.response.CurrentUserResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 현재 로그인한 사용자(userId 기준)의 프로필 정보 조회
     */
    public CurrentUserResponse getCurrentUser(Long userId) {
        UserDto user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return CurrentUserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
