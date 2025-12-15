package com.popspot.popupplatform.controller;

import com.popspot.popupplatform.dto.MainPageResponse;
import com.popspot.popupplatform.dto.main.MainRecommendResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.main.MainPageService;
import com.popspot.popupplatform.service.main.MainRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainPageController {

    private final MainPageService mainPageService;
    private final MainRecommendService service;

    @GetMapping("/popups")
    public MainPageResponse getMainPopups(
            @RequestParam(defaultValue = "4") int limit
    ) {
        return mainPageService.getMainPagePopups(limit);
    }

    @GetMapping("/recommend")
    public MainRecommendResponse recommend(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "4") int limit
    ) {
        Long userId = user != null ? user.getUserId() : null;
        return service.getRecommendedPopups(userId, limit);
    }
}