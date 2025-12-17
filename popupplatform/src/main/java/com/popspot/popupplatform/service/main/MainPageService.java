package com.popspot.popupplatform.service.main;

import com.popspot.popupplatform.dto.MainPageResponse;

public interface MainPageService {
    MainPageResponse getMainPagePopups(int limit);
}
