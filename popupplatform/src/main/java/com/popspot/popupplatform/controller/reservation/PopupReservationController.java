package com.popspot.popupplatform.controller.reservation;

import com.popspot.popupplatform.dto.reservation.request.PopupReservationSettingRequest;
import com.popspot.popupplatform.dto.reservation.response.PopupReservationSettingResponse;
import com.popspot.popupplatform.service.reservation.PopupReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/popup")
@RequiredArgsConstructor
@Tag(name = "Popup Reservation", description = "팝업 예약 설정 API (단위 시간, 요일별 시간표, 제외일)")
public class PopupReservationController {

    private final PopupReservationService popupReservationService;

    @Operation(summary = "팝업 예약 설정 저장(전체 덮어쓰기)")
    @PostMapping("/{popId}/reservation-setting")
    public ResponseEntity<PopupReservationSettingResponse> saveReservationSetting(
            @Parameter(description = "팝업 ID", example = "1")
            @PathVariable Long popId,
            @RequestBody PopupReservationSettingRequest request
    ) {
        PopupReservationSettingResponse response =
                popupReservationService.saveReservationSetting(popId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "팝업 예약 설정 조회")
    @GetMapping("/{popId}/reservation-setting")
    public ResponseEntity<PopupReservationSettingResponse> getReservationSetting(
            @Parameter(description = "팝업 ID", example = "1")
            @PathVariable Long popId
    ) {
        PopupReservationSettingResponse response =
                popupReservationService.getReservationSetting(popId);

        return ResponseEntity.ok(response);
    }
}
