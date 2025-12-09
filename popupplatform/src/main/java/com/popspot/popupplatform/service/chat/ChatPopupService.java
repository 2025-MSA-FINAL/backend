package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.chat.response.PopupSimpleListResponse;
import com.popspot.popupplatform.dto.chat.response.PopupSimpleResponse;
import com.popspot.popupplatform.mapper.chat.ChatPopupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatPopupService {
    private final ChatPopupMapper chatPopupMapper;
    public PopupSimpleListResponse getPopupList(String keyword) {
        //모든 팝업이름 목록으로 가져와서 list 저장
        List<PopupSimpleResponse> list;

        if (keyword != null && !keyword.isBlank()) {
            list = chatPopupMapper.searchPopupNames(keyword);
        } else {
            list = chatPopupMapper.findAllPopupNames();
        }

        return PopupSimpleListResponse.builder()
                .count(list.size()) //list 개수
                .popups(list)
                .build();
    }
}