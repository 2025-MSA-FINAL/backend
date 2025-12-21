package com.popspot.popupplatform.controller.chat;

import com.popspot.popupplatform.dto.chat.request.ChatReportRequest;
import com.popspot.popupplatform.global.security.CustomUserDetails;
import com.popspot.popupplatform.global.service.ObjectStorageService;
import com.popspot.popupplatform.global.utils.HeicConverter;
import com.popspot.popupplatform.service.chat.ChatReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat Report", description = "채팅 신고 API")
public class ChatReportController {
    private final ChatReportService chatReportService;
    private final ObjectStorageService objectStorageService;

    @Operation(summary = "채팅/유저 신고", description = "이미지 증거가 필수인 채팅/유저 신고 API")
    @PostMapping("/reports")
    public ResponseEntity<Void> report(
            @RequestBody ChatReportRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
       chatReportService.report(req, user);
       return ResponseEntity.ok().build();
    }

    @PostMapping("/reports/upload")
    public ResponseEntity<List<String>> uploadReportImages(
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<String> urls = files.stream().map(file -> {
                try {
                    String contentType = file.getContentType();
                    String originalName = file.getOriginalFilename();

                    boolean isHeic =
                            (contentType != null && contentType.contains("heic")) ||
                                    (originalName != null && originalName.toLowerCase().endsWith(".heic")) ||
                                    (originalName != null && originalName.toLowerCase().endsWith(".heif"));

                    // HEIC → JPG 변환
                    if (isHeic) {
                        File tempHeic = File.createTempFile("report-", ".heic");
                        file.transferTo(tempHeic);

                        File jpg = HeicConverter.convertHeicToJpg(tempHeic);
                        byte[] bytes = Files.readAllBytes(jpg.toPath());

                        return objectStorageService.uploadBytes(
                                "report/images",
                                bytes,
                                "image/jpeg",
                                "jpg"
                        ).getUrl();
                    }

                    // 일반 이미지
                    return objectStorageService
                            .upload("report/images", file)
                            .getUrl();

                } catch (Exception e) {
                    throw new RuntimeException("신고 이미지 처리 실패", e);
                }
            }).toList();

            return ResponseEntity.ok(urls);

        } catch (Exception e) {
            throw new RuntimeException("신고 이미지 업로드 실패", e);
        }
    }
}
