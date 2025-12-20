package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.ChatReportRequest;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.ChatReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat Report", description = "채팅 신고 API")
public class ChatReportController {
    private final ChatReportService chatReportService;

    @Operation(summary = "채팅/유저 신고", description = "이미지 증거가 필수인 채팅/유저 신고 API")
    @PostMapping("/reports")
    public ResponseEntity<Void> report(
            @RequestBody ChatReportRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
       chatReportService.report(req, user);
       return ResponseEntity.ok().build();
    }
}
