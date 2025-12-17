package com.popspot.popupplatform.controller;

import com.popspot.popupplatform.dto.MainPageResponse;
import com.popspot.popupplatform.service.main.MainPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/main")
public class MainPageController {

    private final MainPageService mainPageService;

    @GetMapping("/popups")
    public MainPageResponse getMainPopups(
            @RequestParam(defaultValue = "4") int limit
    ) {
        return mainPageService.getMainPagePopups(limit);
    }
}