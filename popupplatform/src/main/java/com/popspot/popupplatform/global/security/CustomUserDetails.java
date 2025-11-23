// src/main/java/com/popspot/popupplatform/global/security/CustomUserDetails.java
package com.popspot.popupplatform.global.security;

import com.popspot.popupplatform.dto.user.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security에서 사용할 사용자 정보 구현체.
 * - JWT 기반 인증에서 principal로 사용된다.
 * - 비밀번호는 JWT 인증에서 사용하지 않으므로 포함하지 않는다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String role;    // 예: USER / MANAGER / ADMIN
    private final String status;  // 예: ACTIVE / PENDING / DELETED

    public CustomUserDetails(Long userId,
                             String role,
                             String status) {
        this.userId = userId;
        this.role = role;
        this.status = status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // DB role 값을 기준으로 ROLE_ prefix 부여
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        // JWT 인증만 사용하므로 비밀번호는 사용하지 않는다.
        return null;
    }

    @Override
    public String getUsername() {
        // 유니크한 식별자로 userId를 사용
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 필요 시 UserStatus 기반으로 세분화 가능
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 필요 시 잠금 상태 추가 가능
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // JWT 기반이라 자격증명 만료는 따로 보지 않음
    }

    /**
     * UserStatus.ACTIVE 인 경우에만 활성 계정으로 취급
     */
    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.name().equalsIgnoreCase(this.status);
    }
}
