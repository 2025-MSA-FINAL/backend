package com.popspot.popupplatform.global.security;

import com.popspot.popupplatform.dto.global.JwtUserDto;
import com.popspot.popupplatform.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * JWT 필터 등에서 사용할 UserDetailsService 구현체.
 * - subject(userId)를 기준으로 DB에서 계정을 조회한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long userId = Long.valueOf(username);

        JwtUserDto user = userMapper.findJwtUserByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(
                user.getUserId(),
                user.getRole(),
                user.getStatus()
        );
    }

    /**
     * userId(Long)을 직접 받아서 조회할 수 있도록 하는 헬퍼 메서드
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        return loadUserByUsername(String.valueOf(userId));
    }
}
