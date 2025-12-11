package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.AdminUserDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.mapper.admin.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;

    @Override
    public PageDTO<AdminUserDTO> getUserList(String status, PageRequestDTO pageRequest) {
        log.info("유저 목록 조회 - status: {}, page: {}, size: {}",
                status, pageRequest.getPage(), pageRequest.getSize());

        List<AdminUserDTO> userList = adminUserMapper.getUserList(
                status,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        int totalCount = adminUserMapper.getUserCount(status);

        return new PageDTO<>(
                userList,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalCount
        );
    }

    @Override
    public PageDTO<AdminUserDTO> getManagerList(String status, PageRequestDTO pageRequest) {
        log.info("매니저 목록 조회 - status: {}, page: {}, size: {}",
                status, pageRequest.getPage(), pageRequest.getSize());

        List<AdminUserDTO> managerList = adminUserMapper.getManagerList(
                status,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );

        int totalCount = adminUserMapper.getManagerCount(status);

        return new PageDTO<>(
                managerList,
                pageRequest.getPage(),
                pageRequest.getSize(),
                totalCount
        );
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        log.info("유저 상태 변경 - userId: {}, status: {}", userId, status);

        if (!status.equals("ACTIVE") && !status.equals("DELETED")) {
            throw new IllegalArgumentException("잘못된 상태 값입니다: " + status);
        }

        int result = adminUserMapper.updateUserStatus(userId, status);
        if (result == 0) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다: " + userId);
        }
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, String role) {
        log.info("유저 권한 변경 - userId: {}, role: {}", userId, role);

        if (!role.equals("USER") && !role.equals("MANAGER")) {
            throw new IllegalArgumentException("잘못된 권한 값입니다: " + role);
        }

        int result = adminUserMapper.updateUserRole(userId, role);
        if (result == 0) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다: " + userId);
        }
    }
}