package com.popspot.popupplatform.controller.admin;

import com.popspot.popupplatform.dto.admin.AIReportResponseDTO;
import com.popspot.popupplatform.service.admin.AIReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j  // ← 추가
@Tag(name = "Admin AI Report", description = "관리자용 AI 기반 운영 리포트 API")
@RestController
@RequestMapping("/api/admin/ai-reports")
@RequiredArgsConstructor
public class AIReportController {

    private final AIReportService aiReportService;

    @Operation(
            summary = "월간 AI 리포트 생성",
            description = "내부 데이터 신뢰도 + 외부 트렌드 기반 AI 분석 리포트 생성. 기간이 제공되지 않으면 현재 월을 기준으로 생성."
    )
    @GetMapping("/monthly")
    public ResponseEntity<AIReportResponseDTO> generateMonthlyReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            log.info("🎯 [Controller] AI 리포트 생성 요청 받음");
            log.info("   - startDate: {}", startDate);
            log.info("   - endDate: {}", endDate);

            AIReportResponseDTO response = aiReportService.generateMonthlyReport(startDate, endDate);

            log.info("✅ [Controller] AI 리포트 생성 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ [Controller] AI 리포트 생성 실패", e);
            log.error("❌ [Controller] 에러 메시지: {}", e.getMessage());
            log.error("❌ [Controller] 에러 타입: {}", e.getClass().getName());

            // 스택 트레이스 전체 출력
            e.printStackTrace();

            throw e;  // GlobalExceptionHandler로 전달
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLatestReport() {
        AIReportResponseDTO latest = aiReportService.getLatestAIReport();
        String filePath = latest.getAirPdfUrl();

        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}