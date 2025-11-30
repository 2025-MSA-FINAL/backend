// src/main/java/com/popspot/popupplatform/controller/user/MyPageController.java
package com.popspot.popupplatform.controller.user;

import com.popspot.popupplatform.dto.common.PageDTO;
import com.popspot.popupplatform.dto.common.PageRequestDTO;
import com.popspot.popupplatform.dto.user.ReservationListItemDto;
import com.popspot.popupplatform.dto.user.WishlistItemDto;
import com.popspot.popupplatform.dto.user.enums.ReservationStatusFilter;
import com.popspot.popupplatform.dto.user.enums.WishlistStatusFilter;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.user.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 예약/찜 리스트 API")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "내 예약 리스트 조회 (페이지네이션)")
    @GetMapping("/reservations")
    public ResponseEntity<PageDTO<ReservationListItemDto>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute PageRequestDTO pageRequest,   // ?page=0&size=6&sortDir=DESC
            @RequestParam(name = "status", defaultValue = "ALL")
            ReservationStatusFilter status
    ) {
        PageDTO<ReservationListItemDto> result =
                myPageService.getMyReservations(user.getUserId(), pageRequest, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "내 찜 리스트 조회 (페이지네이션)")
    @GetMapping("/wishlist")
    public ResponseEntity<PageDTO<WishlistItemDto>> getMyWishlist(
            @AuthenticationPrincipal CustomUserDetails user,
            @ModelAttribute PageRequestDTO pageRequest,   // ?page=0&size=6&sortDir=DESC
            @RequestParam(name = "status", defaultValue = "ALL")
            WishlistStatusFilter status
    ) {
        PageDTO<WishlistItemDto> result =
                myPageService.getMyWishlist(user.getUserId(), pageRequest, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "찜한 팝업 목록 전체 삭제")
    @DeleteMapping("/wishlist")
    public ResponseEntity<Boolean> deleteAllWishlist(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(myPageService.deleteAllWishList(user.getUserId()));
    }

    @Operation(summary = "찜한 목록 중 종료된 팝업 전체 삭제")
    @DeleteMapping("/wishlist/close")
    public ResponseEntity<Boolean> deleteCloseWishlist(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(myPageService.deleteCloseWishList(user.getUserId()));
    }
}
