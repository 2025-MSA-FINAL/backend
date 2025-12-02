package com.popspot.popupplatform.controller.popup;

import com.popspot.popupplatform.dto.popup.request.PopupCreateRequest;
import com.popspot.popupplatform.dto.popup.request.PopupListRequest;
import com.popspot.popupplatform.dto.popup.response.PopupDetailResponse;
import com.popspot.popupplatform.dto.popup.response.PopupListResponse;
import com.popspot.popupplatform.dto.popup.response.PopupWishlistToggleResponse;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.service.popup.PopupService;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.AuthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/popups")
@RequiredArgsConstructor
@Tag(name = "Popup", description = "팝업 스토어 관리 API")
public class PopupController {

    private final PopupService popupService;

    /**
     * 팝업 스토어 등록
     * - [POST] /api/popups
     * - Content-Type: application/json
     * - Body: PopupCreateRequest (이미지 URL 포함)
     */
    @Operation(summary = "팝업 스토어 등록", description = "이미지 업로드 API에서 받은 URL을 포함하여 팝업을 등록합니다. (매니저 권한 필요)")
    @ApiResponse(responseCode = "201", description = "팝업 등록 성공")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createPopup(


            @RequestBody @Valid PopupCreateRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 로그인 체크
        if (userDetails == null) {
            throw new CustomException(AuthErrorCode.NO_AUTH_TOKEN);
        }

        // 2. ID 추출
        Long managerId = Long.parseLong(userDetails.getUsername());

        log.info("팝업 등록 요청: managerId={}, title={}", managerId, request.getPopName());

        // 3. 서비스 호출 (파일 파라미터 없이 DTO와 매니저ID만 전달)
        Long popupId = popupService.registerPopup(request, managerId);

        // 4. 201 Created 응답 + Location 헤더
        return ResponseEntity.created(URI.create("/api/popups/" + popupId)).build();
    }


    /**
     * 팝업 목록 조회
     * - [GET] /api/popups
     * - QueryString: PopupListRequest (검색, 필터, 정렬, 커서 기반 무한 스크롤)
     * - 비로그인도 조회 가능 (로그인 시 isLiked 포함)
     */
    @Operation(
            summary = "팝업 목록 조회",
            description = "검색/필터/정렬/커서 기반으로 팝업 목록을 조회합니다. 로그인 시 각 팝업의 찜 여부(isLiked)를 함께 내려줍니다."
    )
    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    @GetMapping
    public ResponseEntity<PopupListResponse> getPopupList(

            // QueryString -> DTO 바인딩 (검색/필터/정렬/커서)
            @ModelAttribute PopupListRequest request,

            // 로그인 유저 정보 (없으면 null)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 로그인 유저 ID 추출 (비로그인 허용)
        Long userId = null;
        if (userDetails != null) {
            userId = Long.parseLong(userDetails.getUsername());
        }

        log.info("팝업 목록 조회 요청: userId={}, cursor={}, size={}, sort={}",
                userId,
                request.getCursor(),
                request.getSize(),   // 내부에선 getSafeSize() 사용
                request.getSort()
        );

        log.info("팝업 목록 조회: userDetails={}, userId={}", userDetails, userId);

        // 2. 서비스 호출 (목록 + isLiked 포함)
        PopupListResponse response = popupService.getPopupList(request, userId);

        // 3. 200 OK 응답
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "팝업 찜 토글", description = "로그인 유저 기준으로 해당 팝업을 찜/찜취소 합니다.")
    @ApiResponse(responseCode = "200", description = "찜 상태 변경 성공")
    @PostMapping("/{popupId}/wishlist")
    public ResponseEntity<PopupWishlistToggleResponse> toggleWishlist(

            @PathVariable Long popupId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 로그인 체크
        if (userDetails == null) {
            throw new CustomException(AuthErrorCode.NO_AUTH_TOKEN);
        }

        // 2. userId 추출 (지금 구조에서는 username = userId 문자열)
        Long userId = Long.parseLong(userDetails.getUsername());

        // 3. 서비스 호출 (토글 후 최종 isLiked 반환)
        boolean isLiked = popupService.toggleWishlist(popupId, userId);

        // 4. 200 OK + 현재 찜 여부 응답
        return ResponseEntity.ok(new PopupWishlistToggleResponse(isLiked));
    }


    @Operation(
            summary = "팝업 상세 조회",
            description = "팝업 ID로 상세 정보를 조회합니다. (조회수 증가 포함)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = PopupDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 팝업 ID"
            )
    })
    @GetMapping("/{popupId}")
    public ResponseEntity<PopupDetailResponse> getPopupDetail(

            @Parameter(description = "조회할 팝업 ID", required = true)
            @PathVariable Long popupId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 유저 ID 추출 (비로그인 허용 -> null)
        Long userId = null;
        if (userDetails != null) {
            userId = Long.parseLong(userDetails.getUsername());
        }

        log.info("팝업 상세 조회 요청: popupId={}, userId={}", popupId, userId);

        // 2. 서비스 호출
        PopupDetailResponse response = popupService.getPopupDetail(popupId, userId);

        // 3. 응답
        return ResponseEntity.ok(response);
    }



}