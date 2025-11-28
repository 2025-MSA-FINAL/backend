package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.UserListDTO;
import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;

    /**
     * 일반 유저 목록 조회 (페이지네이션)
     * GET /api/admin/users?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping
    public ResponseEntity<PageDTO<UserListDTO>> getUserList(PageRequestDTO pageRequest) {
        PageDTO<UserListDTO> users = userService.getUserList(pageRequest);
        return ResponseEntity.ok(users);
    }

    /**
     * 매니저 목록 조회 (페이지네이션)
     * GET /api/admin/users/managers?page=0&size=10&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/managers")
    public ResponseEntity<PageDTO<UserListDTO>> getManagerList(PageRequestDTO pageRequest) {
        PageDTO<UserListDTO> managers = userService.getManagerList(pageRequest);
        return ResponseEntity.ok(managers);
    }

    /**
     * 유저 상세 조회
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserListDTO> getUserDetail(@PathVariable Long userId) {
        UserListDTO user = userService.getUserDetail(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 유저 상태 변경 (활성/정지/삭제)
     * PUT /api/admin/users/{userId}/status?status=active
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        boolean success = userService.updateUserStatus(userId, status);
        return success ? ResponseEntity.ok("updated") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 유저 권한 변경 (user/manager/admin)
     * PUT /api/admin/users/{userId}/role?role=manager
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        boolean success = userService.updateUserRole(userId, role);
        return success ? ResponseEntity.ok("updated") : ResponseEntity.badRequest().body("fail");
    }

    /**
     * 유저 검색 (이름, 닉네임, 이메일)
     * GET /api/admin/users/search?keyword=홍길동&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<PageDTO<UserListDTO>> searchUsers(
            @RequestParam String keyword,
            PageRequestDTO pageRequest) {
        PageDTO<UserListDTO> users = userService.searchUsers(keyword, pageRequest);
        return ResponseEntity.ok(users);
    }
}