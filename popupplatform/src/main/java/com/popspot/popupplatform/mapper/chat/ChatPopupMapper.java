package com.popspot.popupplatform.mapper.chat;

import com.popspot.popupplatform.dto.chat.response.PopupSimpleResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatPopupMapper {
    //간단 팝업 전체 목록 불러오기
    List<PopupSimpleResponse> findAllPopupNames();
    //팝업 검색 목록 불러오기
    List<PopupSimpleResponse> searchPopupNames(String keyword);
}