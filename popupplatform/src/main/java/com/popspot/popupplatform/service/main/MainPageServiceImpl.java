package com.popspot.popupplatform.service.main;

import com.popspot.popupplatform.dto.MainPageResponse;
import com.popspot.popupplatform.mapper.main.MainPopupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MainPageServiceImpl implements MainPageService {

    private final MainPopupMapper mainPopupMapper;

    @Transactional(readOnly = true)
    @Override
    public MainPageResponse getMainPagePopups(int limit) {
        return MainPageResponse.builder()
                .hero(mainPopupMapper.selectTopViewedPopups(7))
                .latest(mainPopupMapper.selectLatestPopups(limit))
                .endingSoon(mainPopupMapper.selectEndingSoonPopups(limit))
                .openingSoon(mainPopupMapper.selectOpeningSoonPopups(limit))
                .build();
    }

}