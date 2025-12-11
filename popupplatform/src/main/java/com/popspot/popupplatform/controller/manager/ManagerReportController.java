package com.popspot.popupplatform.controller.manager;

import com.popspot.popupplatform.dto.manager.ManagerReportResponseDto;
import com.popspot.popupplatform.service.manager.ManagerReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Manager Report", description = "매니저용 팝업 통계 및 리포트 API")
@RestController
@RequestMapping("/api/manager/popups")
@RequiredArgsConstructor
public class ManagerReportController {

    private final ManagerReportService managerReportService;

    @Operation(summary = "매니저 리포트 조회", description = "해당 팝업의 KPI, 방문객 통계, 시장 분석(Top 5 태그), AI 인사이트를 통합 조회합니다.")
    @GetMapping("/{popupId}/report")
    public ResponseEntity<ManagerReportResponseDto> getManagerReport(@PathVariable("popupId") Long popupId) {
        log.info("매니저 리포트 조회 요청 - popupId: {}", popupId);

        ManagerReportResponseDto report = managerReportService.getManagerReport(popupId);

        return ResponseEntity.ok(report);
    }
}