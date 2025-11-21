// src/main/java/com/popspot/popupplatform/service/auth/UserDuplicationService.java
package com.popspot.popupplatform.service.auth;

import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 회원가입 시 이메일 / 아이디 / 닉네임 중복 여부를 확인하는 서비스
 */
@Service
@RequiredArgsConstructor
public class UserDuplicationService {

    private final UserMapper userMapper;

    /**
     * 이메일 중복 여부
     * @return true면 이미 사용 중(중복), false면 사용 가능
     */
    public boolean isEmailDuplicate(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userMapper.countByEmail(email) > 0;
    }

    /**
     * 로그인 아이디 중복 여부
     * @return true면 이미 사용 중(중복), false면 사용 가능
     */
    public boolean isLoginIdDuplicate(String loginId) {
        if (!StringUtils.hasText(loginId)) {
            return false;
        }
        return userMapper.countByLoginId(loginId) > 0;
    }

    /**
     * 닉네임 중복 여부
     * @return true면 이미 사용 중(중복), false면 사용 가능
     */
    public boolean isNicknameDuplicate(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return false;
        }
        return userMapper.countByNickname(nickname) > 0;
    }
}
