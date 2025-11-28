package com.popspot.popupplatform.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserWishlistMapper {

    //이미 찜했는지 여부 (0 또는 1)
    int existsByUserIdAndPopId(@Param("userId") Long userId,
                               @Param("popId") Long popId);

    //찜 추가
    void insertWishlist(@Param("userId") Long userId,
                        @Param("popId") Long popId);

    //찜 삭제
    void deleteWishlist(@Param("userId") Long userId,
                        @Param("popId") Long popId);

    //목록 isLiked용
    List<Long> findLikedPopupIds(@Param("userId") Long userId,
                                 @Param("popupIds") List<Long> popupIds);
}
