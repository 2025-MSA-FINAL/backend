package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.UserListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.admin.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper userMapper;

    @Override
    public PageDTO<UserListDTO> getUserList(PageRequestDTO pageRequest) {
        List<UserListDTO> users = userMapper.findUserList(pageRequest);
        long total = userMapper.countUserList();
        return new PageDTO<>(users, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public PageDTO<UserListDTO> getManagerList(PageRequestDTO pageRequest) {
        List<UserListDTO> managers = userMapper.findManagerList(pageRequest);
        long total = userMapper.countManagerList();
        return new PageDTO<>(managers, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public UserListDTO getUserDetail(Long userId) {
        return userMapper.findUserById(userId);
    }

    @Override
    public boolean updateUserStatus(Long userId, String status) {
        return userMapper.updateUserStatus(userId, status) > 0;
    }

    @Override
    public boolean updateUserRole(Long userId, String role) {
        return userMapper.updateUserRole(userId, role) > 0;
    }

    @Override
    public PageDTO<UserListDTO> searchUsers(String keyword, PageRequestDTO pageRequest) {
        List<UserListDTO> users = userMapper.searchUsers(keyword, pageRequest);
        long total = userMapper.countSearchUsers(keyword);
        return new PageDTO<>(users, pageRequest.getPage(), pageRequest.getSize(), total);
    }
}
