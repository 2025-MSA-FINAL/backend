package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminUserDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;



public interface AdminUserService {

    PageDTO<AdminUserDTO> getUserList(String status, PageRequestDTO pageRequest);

    PageDTO<AdminUserDTO> getManagerList(String status, PageRequestDTO pageRequest);

    void updateUserStatus(Long userId, String status); //유저 상태 변경 (ACTIVE <-> DELETED)

    void updateUserRole(Long userId, String role); // 유저 권한 변경 (USER <-> MANAGER)
}










