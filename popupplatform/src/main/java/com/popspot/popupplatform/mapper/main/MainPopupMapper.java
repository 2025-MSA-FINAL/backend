package com.popspot.popupplatform.mapper.main;

import com.popspot.popupplatform.dto.MainPageResponse;
import com.popspot.popupplatform.dto.main.MainPopupCardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MainPopupMapper {
    List<MainPopupCardDto> selectLatestPopups(@Param("limit") int limit);
    List<MainPopupCardDto> selectEndingSoonPopups(@Param("limit") int limit);
    List<MainPopupCardDto> selectTopViewedPopups(@Param("limit") int limit);
    List<MainPopupCardDto> selectOpeningSoonPopups(@Param("limit") int limit);
}
