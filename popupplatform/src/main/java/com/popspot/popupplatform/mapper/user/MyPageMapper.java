// src/main/java/com/popspot/popupplatform/mapper/user/MyPageMapper.java
package com.popspot.popupplatform.mapper.user;

import com.popspot.popupplatform.dto.user.ReservationListItemDto;
import com.popspot.popupplatform.dto.user.WishlistItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyPageMapper {

    // ===== 예약 리스트 (페이지네이션) =====
    List<ReservationListItemDto> findReservationsByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("reserveStatusFilter") String reserveStatusFilter,  // "ALL", "CONFIRMED", "CANCELLED"
            @Param("sortDir") String sortDir                           // "ASC" / "DESC"
    );

    long countReservationsByUser(
            @Param("userId") Long userId,
            @Param("reserveStatusFilter") String reserveStatusFilter
    );

    // ===== 찜 리스트 (페이지네이션) =====
    List<WishlistItemDto> findWishlistByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("popupStatusFilter") String popupStatusFilter,      // "ALL", "UPCOMING", "ONGOING", "ENDED"
            @Param("sortDir") String sortDir                           // "ASC" / "DESC"
    );

    long countWishlistByUser(
            @Param("userId") Long userId,
            @Param("popupStatusFilter") String popupStatusFilter
    );

    long deleteAllWishList(@Param("userId") Long userId);

    long deleteCloseWishList(@Param("userId") Long userId,@Param("popStatus") String popStatus);
}
