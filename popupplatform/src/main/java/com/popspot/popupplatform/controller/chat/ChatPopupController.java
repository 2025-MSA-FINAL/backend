package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.response.PopupSimpleListResponse;
import com.popspot.popupplatform.dto.chat.response.PopupSimpleResponse;
import com.popspot.popupplatform.service.chat.ChatPopupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/popup")
public class ChatPopupController {

    private final ChatPopupService chatPopupService;

    @GetMapping("/list")
    @Operation(
            summary = "전체 팝업 리스트 조회",
            description = "채팅에서 사용할 팝업 이름 목록을 최신순(created_at DESC)으로 조회합니다."
    )
    public ResponseEntity<PopupSimpleListResponse> getPopupList(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(chatPopupService.getPopupList(keyword));
    }
}
