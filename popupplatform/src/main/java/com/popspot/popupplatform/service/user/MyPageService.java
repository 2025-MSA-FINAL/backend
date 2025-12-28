// src/main/java/com/popspot/popupplatform/service/user/MyPageService.java
package com.popspot.popupplatform.service.user;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.popup.enums.PopupStatus;
import com.popspot.popupplatform.dto.user.ReservationListItemDto;
import com.popspot.popupplatform.dto.user.WishlistItemDto;
import com.popspot.popupplatform.dto.user.enums.ReservationStatusFilter;
import com.popspot.popupplatform.dto.user.enums.WishlistStatusFilter;
import com.popspot.popupplatform.mapper.user.MyPageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MyPageMapper myPageMapper;

    public PageDTO<ReservationListItemDto> getMyReservations(
            Long userId,
            PageRequestDTO pageRequest,
            ReservationStatusFilter statusFilter
    ) {
        int page = Math.max(pageRequest.getPage(), 0);
        int size = Math.max(pageRequest.getSize(), 1);

        int offset = page * size;
        String sortDir = pageRequest.getSortDir();

        ReservationStatusFilter effectiveFilter =
                statusFilter != null ? statusFilter : ReservationStatusFilter.ALL;

        List<ReservationListItemDto> content =
                myPageMapper.findReservationsByUser(
                        userId,
                        offset,
                        size,
                        effectiveFilter.name(),
                        sortDir
                );

        long total = myPageMapper.countReservationsByUser(
                userId,
                effectiveFilter.name()
        );

        return new PageDTO<>(content, page, size, total);
    }

    public PageDTO<WishlistItemDto> getMyWishlist(
            Long userId,
            PageRequestDTO pageRequest,
            WishlistStatusFilter statusFilter
    ) {
        int page = Math.max(pageRequest.getPage(), 0);
        int size = Math.max(pageRequest.getSize(), 1);

        int offset = page * size;
        String sortDir = pageRequest.getSortDir();

        WishlistStatusFilter effectiveFilter =
                statusFilter != null ? statusFilter : WishlistStatusFilter.ALL;

        List<WishlistItemDto> content =
                myPageMapper.findWishlistByUser(
                        userId,
                        offset,
                        size,
                        effectiveFilter.name(),
                        sortDir
                );

        long total = myPageMapper.countWishlistByUser(
                userId,
                effectiveFilter.name()
        );

        return new PageDTO<>(content, page, size, total);
    }

    @Transactional
    public Boolean deleteAllWishList(Long userId) {
        return myPageMapper.deleteAllWishList(userId) > 0;
    }

    @Transactional
    public Boolean deleteCloseWishList(Long userId) {
        return myPageMapper.deleteCloseWishList(userId, PopupStatus.ENDED.name()) > 0 ;
    }
}
