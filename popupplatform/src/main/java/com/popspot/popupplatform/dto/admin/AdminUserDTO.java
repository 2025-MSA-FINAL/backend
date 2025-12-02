package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자 - 유저/매니저 목록 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private Long userId;
    private String userName;
    private String userNickname;
    private String userEmail;
    private String userPhonenumber;
    private String userGender;
    private Integer userBirthyear;
    private String userStatus;      // ACTIVE, DELETED
    private String userRole;        // USER, MANAGER
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // 탈퇴일로 사용
}