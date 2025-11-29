package com.popspot.popupplatform.service.user;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import com.popspot.popupplatform.dto.user.ReservationListItemDto;
import com.popspot.popupplatform.dto.user.WishlistItemDto;
import com.popspot.popupplatform.mapper.user.MyPageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MyPageMapper myPageMapper;

    public PageDTO<ReservationListItemDto> getMyReservations(Long userId, PageRequestDTO pageRequest) {
        int page = Math.max(pageRequest.getPage(), 0);  // 0부터 시작
        int size = Math.max(pageRequest.getSize(), 1);  // 최소 1

        int offset = page * size;

        List<ReservationListItemDto> content =
                myPageMapper.findReservationsByUser(userId, offset, size);

        long total = myPageMapper.countReservationsByUser(userId);

        return new PageDTO<>(content, page, size, total);
    }

    public PageDTO<WishlistItemDto> getMyWishlist(Long userId, PageRequestDTO pageRequest) {
        int page = Math.max(pageRequest.getPage(), 0);
        int size = Math.max(pageRequest.getSize(), 1);

        int offset = page * size;

        List<WishlistItemDto> content =
                myPageMapper.findWishlistByUser(userId, offset, size);

        long total = myPageMapper.countWishlistByUser(userId);

        return new PageDTO<>(content, page, size, total);
    }

    public Boolean deleteAllWishList(Long userId) {
       return myPageMapper.deleteAllWishList(userId) > 0;
    }

    public Boolean deleteCloseWishList(Long userId) {
        return myPageMapper.deleteCloseWishList(userId, PopupStatus.ENDED.name()) > 0 ;
    }
}