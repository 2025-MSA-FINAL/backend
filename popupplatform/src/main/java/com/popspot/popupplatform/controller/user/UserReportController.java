package com.popspot.popupplatform.controller.user;

import com.popspot.popupplatform.dto.user.report.UserPersonaReport;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.user.UserReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 리포트 관련 API")
public class UserReportController {
    private final UserReportService userReportService;

    @Operation(summary = "유저 리포트 조회", description = "현재 로그인한 회원의 정보를 분석하여 리포트를 작성합니다.")
    @GetMapping("/report")
    public ResponseEntity<UserPersonaReport> userReport(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserPersonaReport userPersonaReport = userReportService.userReport(userId);
        return ResponseEntity.ok(userPersonaReport);
    }
}
