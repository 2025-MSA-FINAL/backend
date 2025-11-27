package com.popspot.popupplatform.service.user;

import com.popspot.popupplatform.dto.user.LoginUserDto;
import com.popspot.popupplatform.dto.user.UserDto;
import com.popspot.popupplatform.dto.user.request.ChangePasswordRequest;
import com.popspot.popupplatform.dto.user.request.UpdateEmailRequest;
import com.popspot.popupplatform.dto.user.request.UpdateNicknameRequest;
import com.popspot.popupplatform.dto.user.request.UpdatePhoneRequest;
import com.popspot.popupplatform.dto.user.response.CurrentUserResponse;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.UserErrorCode;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 현재 로그인한 사용자 정보 조회 (마이페이지 상단 / 네비바)
     */
    public CurrentUserResponse getCurrentUser(Long userId) {
        UserDto user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));



        return CurrentUserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .gender(user.getGender())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(Long userId, UpdateNicknameRequest request) {
        UserDto user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        String newNickname = request.getNickname();

        // 동일한 닉네임이면 아무 작업 안 함
        if (newNickname.equals(user.getNickname())) {
            return;
        }

        // 닉네임 중복 체크
        if (userMapper.countByNickname(newNickname) > 0) {
            throw new CustomException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        int updated = userMapper.updateNickname(userId, newNickname);
        if (updated != 1) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 이메일 변경
     */
    public void updateEmail(Long userId, UpdateEmailRequest request) {
        UserDto user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        String newEmail = request.getEmail();

        // 같은 이메일이면 패스
        if (newEmail.equals(user.getEmail())) {
            return;
        }

        // 이메일 중복 체크
        if (userMapper.countByEmail(newEmail) > 0) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        int updated = userMapper.updateEmail(userId, newEmail);
        if (updated != 1) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 휴대폰 번호 변경
     * - 실제 문자 인증은 /api/auth/phone/* API에서 이미 검증했다고 가정
     */
    public void updatePhone(Long userId, UpdatePhoneRequest request) {
        UserDto user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        String newPhone = request.getPhone();

        // 같은 번호면 패스
        if (newPhone.equals(user.getPhone())) {
            return;
        }

        // 휴대폰 중복 체크
        if (userMapper.countByPhone(newPhone) > 0) {
            throw new CustomException(UserErrorCode.DUPLICATE_PHONE);
        }

        int updated = userMapper.updatePhone(userId, newPhone);
        if (updated != 1) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 비밀번호 변경
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // USER_GENERAL + USER 조인 정보
        LoginUserDto loginUser = userMapper.findGeneralUserByUserId(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), loginUser.getPassword())) {
            throw new CustomException(UserErrorCode.INVALID_PASSWORD);
        }

        String encodedNewPwd = passwordEncoder.encode(request.getNewPassword());
        int updated = userMapper.updatePassword(userId, encodedNewPwd);
        if (updated != 1) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     * - USER.user_status = 'DELETED'
     */
    public void deleteUser(Long userId) {
        int updated = userMapper.softDeleteUser(userId);
        if (updated != 1) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }
    }
}
