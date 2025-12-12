// src/main/java/com/popspot/popupplatform/mapper/UserMapper.java
package com.popspot.popupplatform.mapper.user;

import com.popspot.popupplatform.dto.chat.UserLimitInfoDto;
import com.popspot.popupplatform.dto.global.JwtUserDto;
import com.popspot.popupplatform.dto.user.LoginUserDto;
import com.popspot.popupplatform.dto.user.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    /**
     * 소셜 계정(provider + providerId)으로 사용자 조회
     * USER + USER_SOCIAL 조인
     */
    Optional<UserDto> findByProviderAndProviderId(@Param("provider") String provider,
                                                  @Param("providerId") String providerId);

    /**
     * USER 테이블에 기본 사용자 정보 생성
     */
    int insertUser(UserDto user);

    /**
     * USER_SOCIAL 테이블에 소셜 정보 연결
     */
    void insertUserSocial(UserDto user);
    void insertUserGeneral(UserDto user);

    /**
     * 일반 로그인용 계정 조회
     * USER_GENERAL + USER 조인
     */
    Optional<LoginUserDto> findGeneralUserByLoginId(@Param("loginId") String loginId);


    /**
     * userId 기준으로 로그인용 계정 조회 (JWT 인증에서 사용)
     */
    Optional<JwtUserDto> findJwtUserByUserId(@Param("userId") Long userId);
    // ==========================
    // 중복 체크용 메서드들 추가
    // ==========================

    /**
     * userId(PK)로 USER 조회
     * - 네비게이션 바 / 마이페이지에서 현재 로그인한 회원 정보 가져올 때 사용
     */
    Optional<UserDto> findByUserId(@Param("userId") Long userId);


    /**
     * userId 로 로그인 계정 정보 조회 (비밀번호 변경용)
     */
    Optional<LoginUserDto> findGeneralUserByUserId(@Param("userId") Long userId);

    /**
     * 닉네임 변경
     */
    int updateNickname(@Param("userId") Long userId,
                       @Param("nickname") String nickname);

    /**
     * 이메일 변경
     */
    int updateEmail(@Param("userId") Long userId,
                    @Param("email") String email);

    /**
     * 휴대폰 번호 변경
     */
    int updatePhone(@Param("userId") Long userId,
                    @Param("phone") String phone);

    /**
     * 비밀번호 변경 (USER_GENERAL.login_pwd)
     */
    int updatePassword(@Param("userId") Long userId,
                       @Param("password") String encodedPassword);

    /**
     * 회원 탈퇴(소프트 삭제) – 상태만 DELETED 로 변경
     */
    int softDeleteUser(@Param("userId") Long userId);

    /**
     * 휴대폰 번호 중복 개수 조회
     */
    int countByPhone(@Param("phone") String phone);

    /**
     * EMAIL 중복 개수 조회 (USER.user_email 기준)
     */
    int countByEmail(@Param("email") String email);

    /**
     * 로그인 아이디 중복 개수 조회 (USER_GENERAL.login_id 기준)
     */
    int countByLoginId(@Param("loginId") String loginId);

    /**
     * 닉네임 중복 개수 조회 (USER.user_nickname 기준)
     */
    int countByNickname(@Param("nickname") String nickname);

    int updateProfileImage(@Param("userId") Long userId,
                           @Param("profileImageUrl") String profileImageUrl);
    /**
     * 성별/나이 제한 검사용 사용자 기본 정보 조회
     * - GROUP CHAT 입장 시 사용
     * - user_gender, user_birthyear 조회
     */
    Optional<UserLimitInfoDto> findUserLimitInfo(@Param("userId") Long userId);

    /** 채팅에서 사용할 유저 기본 정보 조회 */
    Optional<UserDto> findById(Long userId);

    int updateIntroduction(@Param("userId")Long userId, @Param("introduction")String introduction);
}
