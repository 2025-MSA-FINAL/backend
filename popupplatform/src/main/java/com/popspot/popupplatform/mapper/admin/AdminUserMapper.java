package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.UserListDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserMapper {

    // 일반 유저 목록 (role = 'user')
    List<UserListDTO> findUserList(PageRequestDTO pageRequest);
    long countUserList();

    // 매니저 목록 (role = 'manager')
    List<UserListDTO> findManagerList(PageRequestDTO pageRequest);
    long countManagerList();

    // 유저 상세
    UserListDTO findUserById(@Param("userId") Long userId);

    // 유저 상태 변경
    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    // 유저 권한 변경
    int updateUserRole(@Param("userId") Long userId, @Param("role") String role);

    // 유저 검색
    List<UserListDTO> searchUsers(@Param("keyword") String keyword, @Param("pageRequest") PageRequestDTO pageRequest);
    long countSearchUsers(@Param("keyword") String keyword);
}