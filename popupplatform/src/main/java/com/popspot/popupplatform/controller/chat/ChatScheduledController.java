package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.CreateScheduleRequest;
import com.popspot.popupplatform.dto.chat.request.UpdateScheduleRequest;
import com.popspot.popupplatform.dto.chat.response.ScheduledMessageResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.chat.ChatScheduledMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatScheduledController {
    private final ChatScheduledMessageService chatScheduledMessageService;

    @PostMapping("/schedule")
    public void create(
            @RequestBody CreateScheduleRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        chatScheduledMessageService.createSchedule(
                req.getRoomType(),
                req.getRoomId(),
                user.getUserId(),
                req.getContent(),
                req.getScheduledAt()
        );
    }

    // 예약 목록 조회
    @GetMapping("/schedule")
    public List<ScheduledMessageResponse> getMySchedules(
            @RequestParam(required = false, defaultValue = "PENDING") String status,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return chatScheduledMessageService.getMySchedules(
                user.getUserId(),
                status
        );
    }

    // 예약 수정
    @PutMapping("/schedule/{csmId}")
    public void update(
            @PathVariable Long csmId,
            @RequestBody UpdateScheduleRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        chatScheduledMessageService.updateSchedule(
                csmId,
                user.getUserId(),
                req.getContent(),
                req.getScheduledAt()
        );
    }

    // 예약 취소
    @DeleteMapping("/schedule/{csmId}")
    public void cancel(
            @PathVariable Long csmId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        chatScheduledMessageService.cancelSchedule(
                csmId,
                user.getUserId()
        );
    }


}
