package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.UserListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;

public interface AdminUserService {
    PageDTO<UserListDTO> getUserList(PageRequestDTO pageRequest);
    PageDTO<UserListDTO> getManagerList(PageRequestDTO pageRequest);
    UserListDTO getUserDetail(Long userId);
    boolean updateUserStatus(Long userId, String status);
    boolean updateUserRole(Long userId, String role);
    PageDTO<UserListDTO> searchUsers(String keyword, PageRequestDTO pageRequest);
}











