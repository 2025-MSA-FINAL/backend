package com.popspot.popupplatform.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import com.popspot.popupplatform.domain.admin.AdminAIReport;
import com.popspot.popupplatform.domain.admin.AdminAIReportChart;
import com.popspot.popupplatform.dto.admin.*;
import com.popspot.popupplatform.dto.admin.ai.CategoryQualityReportDTO;
import com.popspot.popupplatform.dto.global.UploadResultDto;
import com.popspot.popupplatform.global.exception.CustomException;
import com.popspot.popupplatform.global.exception.code.CommonErrorCode;
import com.popspot.popupplatform.global.service.ObjectStorageService;
import com.popspot.popupplatform.mapper.admin.AdminAIReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIReportServiceImpl implements AIReportService {

    private static final String REPORT_TITLE_FORMAT = "%d년 %d월 월간 리포트";
    private static final int TOP_HASHTAGS_COUNT = 20;

    private final ChatClient.Builder chatClientBuilder;
    private final HashtagEnrichmentService hashtagEnrichmentService;
    private final AdminDashboardService dashboardService;
    private final AdminAIReportMapper adminAIReportMapper;
    private final ObjectMapper objectMapper;
    private final AdminCategoryAnalysisService adminCategoryAnalysisService;
    private final ObjectStorageService storageService;

    //  폰트 경로를 optional로 변경 (기본값: 빈 문자열)
    @Value("${app.report.font-path:}")
    private String fontPath;

    @Override
    @Transactional
    public AIReportResponseDTO generateMonthlyReport(String startDate, String endDate) {
        try {
            log.info("=== AI 월간 리포트 생성 시작 ===");

            validateConfiguration();

            Period period = resolvePeriod(startDate, endDate);

            // 1. 데이터 수집
            DashboardStatsDTO stats = loadDashboardStats();
            List<HashtagEnrichmentDTO> enrichedHashtags = loadEnrichedHashtags(period);
            Map<String, List<HashtagEnrichmentDTO>> categoryGroups = groupByCategory(enrichedHashtags);
            List<CategoryQualityReportDTO> categoryQualityReports = analyzeCategoryQuality(categoryGroups.keySet());

            // 1-1. KPI 데이터 추출 및 JSON 변환
            Map<String, Object> kpiDataMap = new HashMap<>();
            kpiDataMap.put("stats", stats);
            kpiDataMap.put("hashtags", enrichedHashtags);
            kpiDataMap.put("quality", categoryQualityReports);
            String rawKpiJson = objectMapper.writeValueAsString(kpiDataMap);

            // 2. AI 분석 리포트 생성
            String analysisJson = buildComprehensiveAnalysis(stats, enrichedHashtags, categoryGroups,
                    analyzeDemographics(stats, categoryGroups), categoryQualityReports);
            AIReportResponseDTO report = generateMonthlyAIReport(analysisJson, period);

            // 3. PDF 파일 생성
            String fileName = "Report_" + period.start() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            Map<String, Integer> chartData = categoryQualityReports.stream()
                    .collect(Collectors.toMap(CategoryQualityReportDTO::getCategory, r -> (int)(r.getMatchRate() * 100)));

            byte[] pdfBytes = createPdfReportContent(report, chartData);

            // 4. S3에 PDF 업로드
            UploadResultDto uploadResult = storageService.uploadBytes(
                    "reports", pdfBytes, "application/pdf", "pdf"
            );
            String s3Url = uploadResult.getUrl();
            String s3Key = uploadResult.getKey();

            log.info(" PDF S3 업로드 완료: {}", s3Url);

            // 5. 차트 이미지도 S3 업로드
            byte[] chartBytes = generateChartImage(chartData);
            UploadResultDto chartUpload = storageService.uploadBytes(
                    "charts", chartBytes, "image/png", "png"
            );

            log.info("차트 S3 업로드 완료: {}", chartUpload.getUrl());

            // 6. DB 저장
            saveReportToDB(period.start(), period.end(), analysisJson,
                    s3Url, s3Key, rawKpiJson, chartUpload.getUrl());

            log.info("=== AI 월간 리포트 생성 및 저장 완료 ===");
            report.setAirPdfUrl(s3Url);

            return report;

        } catch (Exception e) {
            log.error(" AI 리포트 생성 실패 - 상세 에러:", e);
            log.error(" 에러 메시지: {}", e.getMessage());
            log.error(" 에러 타입: {}", e.getClass().getName());
            e.printStackTrace();

            throw new RuntimeException("AI 리포트 생성 실패: " + e.getMessage(), e);
        }
    }

    /* ======================================================
        개선: 폰트 로딩 - 여러 방법 시도
       ====================================================== */
    private BaseFont loadFont() throws Exception {
        // 1순위: 설정된 폰트 경로 사용
        if (fontPath != null && !fontPath.isEmpty()) {
            File fontFile = new File(fontPath);
            if (fontFile.exists()) {
                log.info(" 설정된 폰트 사용: {}", fontPath);
                return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } else {
                log.warn("️ 설정된 폰트 파일 없음: {}", fontPath);
            }
        }

        // 2순위: 프로젝트 리소스 폰트 사용
        try {
            String resourcePath = "fonts/NanumGothic.ttf";
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream fontStream = classLoader.getResourceAsStream(resourcePath);

            if (fontStream != null) {
                log.info(" 리소스 폰트 사용: {}", resourcePath);
                byte[] fontData = fontStream.readAllBytes();
                return BaseFont.createFont(
                        "NanumGothic.ttf",
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED,
                        true,
                        fontData,
                        null
                );
            }
        } catch (Exception e) {
            log.debug("리소스 폰트 로드 시도 실패: {}", e.getMessage());
        }

        // 3순위: 시스템 폰트 자동 탐색
        String[] systemFonts = {
                "C:/Windows/Fonts/malgun.ttf",           // Windows - 맑은 고딕
                "C:/Windows/Fonts/MALGUNSL.ttf",         // Windows - 맑은 고딕 Semilight
                "C:/Windows/Fonts/gulim.ttc",            // Windows - 굴림
                "/System/Library/Fonts/AppleGothic.ttf", // Mac - 애플고딕
                "/Library/Fonts/NanumGothic.ttf",        // Mac - 나눔고딕
                "/usr/share/fonts/truetype/nanum/NanumGothic.ttf", // Linux - 나눔고딕
        };

        for (String systemFont : systemFonts) {
            File file = new File(systemFont);
            if (file.exists()) {
                log.info(" 시스템 폰트 자동 탐색: {}", systemFont);
                return BaseFont.createFont(systemFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }

        // 모든 방법 실패
        throw new RuntimeException(
                "한글 폰트를 찾을 수 없습니다. 다음 중 하나를 수행하세요:\n" +
                        "1) src/main/resources/fonts/NanumGothic.ttf 추가\n" +
                        "2) application.yml에 app.report.font-path 설정\n" +
                        "3) 시스템에 한글 폰트 설치 (Windows: 맑은 고딕 기본 포함, Linux: sudo apt-get install fonts-nanum)"
        );
    }

    /* ======================================================
       설정값 검증 (폰트는 선택사항)
       ====================================================== */
    private void validateConfiguration() {
        log.info(" 설정값 검증 시작");

        if (fontPath != null && !fontPath.isEmpty()) {
            log.info(" 폰트 경로 설정됨: {}", fontPath);
        } else {
            log.info(" 폰트 경로 미설정 → 리소스/시스템 폰트 자동 탐색");
        }

        log.info(" 설정값 검증 완료");
    }

    /* ======================================================
       DB 저장 로직
       ====================================================== */
    private void saveReportToDB(LocalDate start, LocalDate end, String contentJson,
                                String s3Url, String s3Key, String rawKpiJson, String chartUrl) {
        String title = String.format(REPORT_TITLE_FORMAT, start.getYear(), start.getMonthValue());

        AdminAIReport entity = AdminAIReport.builder()
                .airType("MONTHLY")
                .airPeriodStart(start)
                .airPeriodEnd(end)
                .airTitle(title)
                .airContent(contentJson)
                .airGeneratedBy("AI")
                .airPdfUrl(s3Url)
                .airStatus("DRAFT")
                .airKpiData(rawKpiJson)
                .build();

        adminAIReportMapper.insertAIReport(entity);

        AdminAIReportChart chartEntity = AdminAIReportChart.builder()
                .airId(entity.getAirId())
                .arcType("CATEGORY_ACCURACY")
                .arcImageUrl(chartUrl)
                .build();

        adminAIReportMapper.insertReportChart(chartEntity);
    }

    /* ======================================================
       PDF 생성 로직
       ====================================================== */
    private byte[] createPdfReportContent(AIReportResponseDTO report, Map<String, Integer> chartData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            //  개선된 폰트 로딩
            log.info(" 폰트 로딩 시도...");
            BaseFont bf = loadFont();
            log.info(" 폰트 로딩 성공");

            Font titleFont = new Font(bf, 22, Font.BOLD, Color.BLACK);
            Font subTitleFont = new Font(bf, 14, Font.BOLD, new Color(63, 81, 181));
            Font bodyFont = new Font(bf, 11, Font.NORMAL, Color.DARK_GRAY);

            Paragraph title = new Paragraph(report.getReportTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            document.add(new Paragraph("1. 핵심 분석 요약", subTitleFont));
            Paragraph summary = new Paragraph(report.getExecutiveSummary(), bodyFont);
            summary.setSpacingBefore(10);
            summary.setSpacingAfter(20);
            document.add(summary);

            document.add(new Paragraph("2. 카테고리별 정합성 지표", subTitleFont));
            byte[] chartImageBytes = generateChartImage(chartData);
            Image chartImage = Image.getInstance(chartImageBytes);
            chartImage.setAlignment(Element.ALIGN_CENTER);
            chartImage.scaleToFit(450, 250);
            chartImage.setSpacingBefore(15);
            document.add(chartImage);

            document.add(Chunk.NEXTPAGE);
            document.add(new Paragraph("3. 영역별 상세 인사이트", subTitleFont));

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15);

            addTableCell(table, "📊 카테고리 분석: " + report.getCategoryInsight(), bodyFont);
            addTableCell(table, "👥 인구통계 분석: " + report.getAudienceInsight(), bodyFont);
            document.add(table);

            document.add(new Paragraph("\n4. 실행 전략 제안", subTitleFont));
            for (String rec : report.getRecommendation()) {
                Paragraph p = new Paragraph("• " + rec, bodyFont);
                p.setIndentationLeft(20);
                document.add(p);
            }

            document.close();
            log.info(" PDF 생성 완료");

        } catch (Exception e) {
            log.error(" PDF 조립 중 에러:", e);
            throw new RuntimeException("PDF 생성 실패", e);
        }
        return baos.toByteArray();
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(12);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private byte[] generateChartImage(Map<String, Integer> chartData) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        chartData.forEach((k, v) -> dataset.addValue(v, "정합성(%)", k));

        JFreeChart chart = ChartFactory.createBarChart(
                "", "카테고리", "점수 (%)",
                dataset, PlotOrientation.VERTICAL, false, true, false
        );
        chart.setBackgroundPaint(Color.WHITE);
        return ChartUtils.encodeAsPNG(chart.createBufferedImage(600, 400));
    }

    /* ======================================================
       헬퍼 메서드들
       ====================================================== */
    private Period resolvePeriod(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) :
                LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) :
                start.withDayOfMonth(start.lengthOfMonth());
        return new Period(start, end);
    }

    private record Period(LocalDate start, LocalDate end) {}

    private DashboardStatsDTO loadDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    private List<HashtagEnrichmentDTO> loadEnrichedHashtags(Period period) {
        return hashtagEnrichmentService.enrichTopHashtags(
                period.start().atStartOfDay(),
                period.end().atTime(23, 59, 59),
                TOP_HASHTAGS_COUNT
        );
    }

    private Map<String, List<HashtagEnrichmentDTO>> groupByCategory(
            List<HashtagEnrichmentDTO> hashtags) {
        return hashtags.stream()
                .collect(Collectors.groupingBy(HashtagEnrichmentDTO::getCategory));
    }

    private Map<String, Object> analyzeDemographics(
            DashboardStatsDTO stats,
            Map<String, List<HashtagEnrichmentDTO>> categoryGroups) {
        Map<String, Object> analysis = new HashMap<>();

        // ✅ null 체크 강화
        if (stats == null || stats.getUserDemographics() == null) {
            log.warn("⚠️ 인구통계 데이터 없음");
            return analysis;
        }

        for (UserDemographicsDTO demo : stats.getUserDemographics()) {
            // ✅ null 체크 추가
            if (demo == null) {
                log.warn("⚠️ UserDemographicsDTO가 null");
                continue;
            }

            // ✅ null-safe 값 처리
            String ageGroup = demo.getAgeGroup() != null ? demo.getAgeGroup() : "UNKNOWN";
            String gender = demo.getGender() != null ? demo.getGender() : "UNKNOWN";
            Long userCount = demo.getUserCount() != 0L ? demo.getUserCount() : 0;

            Map<String, Integer> preferences = new HashMap<>();
            categoryGroups.keySet().forEach(c ->
                    preferences.put(c, categoryGroups.get(c).size())
            );

            // ✅ Map.of() 대신 HashMap 사용
            Map<String, Object> demoData = new HashMap<>();
            demoData.put("ageGroup", ageGroup);
            demoData.put("gender", gender);
            demoData.put("userCount", userCount);
            demoData.put("categoryPreferences", preferences);

            analysis.put(ageGroup + "_" + gender, demoData);
        }

        return analysis;
    }

    private String buildComprehensiveAnalysis(
            DashboardStatsDTO stats,
            List<HashtagEnrichmentDTO> enrichedHashtags,
            Map<String, List<HashtagEnrichmentDTO>> categoryGroups,
            Map<String, Object> demographicAnalysis,
            List<CategoryQualityReportDTO> categoryQualityReports) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("topHashtags", enrichedHashtags);
            data.put("categoryGroups", categoryGroups);
            data.put("categoryQuality", categoryQualityReports);
            data.put("demographics", demographicAnalysis);
            data.put("totalUsers", stats.getTotalUsers());
            data.put("totalPopups", stats.getTotalPopupStores());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    private AIReportResponseDTO generateMonthlyAIReport(String analysisJson, Period period) {
        String prompt = String.format("""
당신은 관리자 대시보드용 월간 AI 분석 리포트를 생성하는 시스템입니다.

⚠️ 반드시 아래 JSON 구조를 정확히 지켜서 출력하세요.
⚠️ 누락된 필드는 허용되지 않습니다.
⚠️ 값이 없으면 null 또는 빈 배열([])을 사용하세요.
⚠️ JSON 외의 텍스트는 절대 출력하지 마세요.

{
  "reportTitle": "string",
  "executiveSummary": "string",
  "audienceInsight": "string",
  "categoryInsight": "string",
  "behaviorInsight": "string",
  "reportConfidence": number,
  "recommendation": ["string", "string"]
}

분석 데이터:
%s
""", analysisJson);

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .temperature(0.3)
                        .build())
                .call()
                .content();

        return parseAIResponse(response);
    }

    private AIReportResponseDTO parseAIResponse(String response) {
        try {
            AIReportResponseDTO dto = objectMapper.readValue(
                    extractJson(response),
                    AIReportResponseDTO.class
            );
            dto.setGeneratedAt(LocalDateTime.now());
            return dto;
        } catch (Exception e) {
            AIReportResponseDTO fallback = new AIReportResponseDTO();
            fallback.setExecutiveSummary(response);
            return fallback;
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        return (start >= 0 && end > start) ? text.substring(start, end + 1) : "{}";
    }

    @Override
    @Transactional(readOnly = true)
    public AIReportResponseDTO getLatestAIReport() {
        return adminAIReportMapper.findLatestAIReport()
                .map(this::processJsonContent)
                .orElseThrow(() -> new CustomException(
                        CommonErrorCode.RESOURCE_NOT_FOUND,
                        "리포트가 없습니다."
                ));
    }

    private AIReportResponseDTO processJsonContent(AIReportResponseDTO dtoFromDb) {
        try {
            AIReportResponseDTO parsed = objectMapper.readValue(
                    dtoFromDb.getAiContentJson(),
                    AIReportResponseDTO.class
            );
            parsed.setReportTitle(dtoFromDb.getReportTitle());
            parsed.setGeneratedAt(dtoFromDb.getGeneratedAt());
            parsed.setAirPdfUrl(dtoFromDb.getAirPdfUrl());
            return parsed;
        } catch (Exception e) {
            return dtoFromDb;
        }
    }

    private List<CategoryQualityReportDTO> analyzeCategoryQuality(Set<String> categories) {
        List<CategoryQualityReportDTO> result = new ArrayList<>();
        for (String category : categories) {
            CategoryValidationDTO stat =
                    adminCategoryAnalysisService.getCategoryValidationStats(category);

            CategoryQualityReportDTO dto = new CategoryQualityReportDTO();
            dto.setCategory(stat.getCategory());
            dto.setTotalTags(stat.getTotalTags());
            dto.setMatchedTags(stat.getMatchedTags());

            double rate = stat.getTotalTags() == 0 ? 0 :
                    (double) stat.getMatchedTags() / stat.getTotalTags();
            dto.setMatchRate(rate);
            dto.setStatus(rate >= 0.75 ? "GOOD" : rate >= 0.5 ? "WARN" : "BAD");

            result.add(dto);
        }
        return result;
    }
}