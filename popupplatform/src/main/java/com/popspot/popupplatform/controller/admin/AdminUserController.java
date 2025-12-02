package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.AdminUserDTO;
import com.popspot.popupplatform.service.admin.AdminUserService;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "관리자 유저/매니저 관리 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @Operation(summary = "유저 목록 조회", description = "USER 역할을 가진 유저 목록을 조회합니다")
    public ResponseEntity<PageDTO<AdminUserDTO>> getUserList(
            @RequestParam(defaultValue = "ACTIVE") String status,
            @ModelAttribute PageRequestDTO pageRequest
    ) {
        log.info("GET /api/admin/users - status: {}, page: {}, size: {}",
                status, pageRequest.getPage(), pageRequest.getSize());

        PageDTO<AdminUserDTO> users = adminUserService.getUserList(status, pageRequest);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/managers")
    @Operation(summary = "매니저 목록 조회", description = "MANAGER 역할을 가진 유저 목록을 조회합니다")
    public ResponseEntity<PageDTO<AdminUserDTO>> getManagerList(
            @RequestParam(defaultValue = "ACTIVE") String status,
            @ModelAttribute PageRequestDTO pageRequest
    ) {
        log.info("GET /api/admin/managers - status: {}, page: {}, size: {}",
                status, pageRequest.getPage(), pageRequest.getSize());

        PageDTO<AdminUserDTO> managers = adminUserService.getManagerList(status, pageRequest);

        return ResponseEntity.ok(managers);
    }


    @PutMapping("/users/{userId}/status")
    @Operation(summary = "유저 상태 변경", description = "유저의 상태를 변경합니다 (ACTIVE/DELETED)")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status
    ) {
        log.info("PUT /api/admin/users/{}/status - status: {}", userId, status);

        adminUserService.updateUserStatus(userId, status);

        return ResponseEntity.ok().build();
    }


    @PutMapping("/users/{userId}/role")
    @Operation(summary = "유저 권한 변경", description = "유저의 권한을 변경합니다 (USER/MANAGER)")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role
    ) {
        log.info("PUT /api/admin/users/{}/role - role: {}", userId, role);

        adminUserService.updateUserRole(userId, role);

        return ResponseEntity.ok().build();
    }
}