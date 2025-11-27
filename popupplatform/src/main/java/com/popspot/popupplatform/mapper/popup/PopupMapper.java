package com.popspot.popupplatform.mapper.popup;

import com.popspot.popupplatform.domain.popup.PopupStore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface PopupMapper {

    //팝업 저장
    void insertPopup(PopupStore popupStore);

    //해시태그 이름으로 조회
    Optional<Long> findHashtagIdByName(@Param("hashName") String hashName);

    //해시태그 저장
    void insertHashtag(@Param("hashName") String hashName);

    //팝업-해시태그 연결
    void insertPopupHashtag(@Param("popId") Long popId,
                            @Param("hashId") Long hashId);

    //상세 이미지 저장 (1:N)
    void insertPopupImage(@Param("popId") Long popId,
                          @Param("imageUrl") String imageUrl,
                          @Param("order") int order);
}