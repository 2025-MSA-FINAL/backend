package com.popspot.popupplatform.global.controller;

import com.popspot.popupplatform.dto.global.UploadResultDto;
import com.popspot.popupplatform.global.service.ObjectStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 업로드 및 삭제 API")
public class FileController {

    private final ObjectStorageService storage;

    @Value("${aws.s3.default-profile.key}")
    private String defaultProfileKey;

    /** 프로필 이미지 업로드: url + key 반환 */
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드하고 URL과 key를 반환합니다.")
    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResultDto> uploadProfile(@RequestParam("file") MultipartFile file) {
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }
        UploadResultDto r = storage.upload("profiles", file);
        return ResponseEntity.ok(new UploadResultDto(r.getUrl(), r.getKey()));
    }



    /** 팝업 이미지 업로드: url + key 반환 */
    @Operation(summary = "팝업 이미지 업로드", description = "팝업 썸네일/상세 이미지를 업로드하고 URL과 key를 반환합니다.")
    @PostMapping(value = "/popup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResultDto> uploadPopup(@RequestParam("file") MultipartFile file) {
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }
        UploadResultDto r = storage.upload("popups", file);
        return ResponseEntity.ok(new UploadResultDto(r.getUrl(), r.getKey()));
    }

    /** 팝업 이미지 여러 장 업로드: url + key 리스트 반환 */
    @Operation(summary = "팝업 이미지 다중 업로드", description = "팝업 이미지를 여러 개 업로드하고 각 URL과 key를 반환합니다.")
    @PostMapping(value = "/popup/list", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UploadResultDto>> uploadPopupList(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<UploadResultDto> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                continue;
            }
            UploadResultDto r = storage.upload("popups", file);
            results.add(new UploadResultDto(r.getUrl(), r.getKey()));
        }
        return ResponseEntity.ok(results);
    }



    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지 key로 이미지를 삭제합니다. 기본 이미지는 삭제되지 않습니다.")
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteProfile(@RequestParam("key") String key) {
        // 기본 이미지라면 삭제 금지
        if (isDefaultKey(key)) {
            return ResponseEntity.noContent().build();
        }

        storage.deleteByKey(key);
        return ResponseEntity.noContent().build();
    }

    private boolean isDefaultKey(String key) {
        if (key == null) return false;
        System.out.println(defaultProfileKey);
        return key.equals(defaultProfileKey)
                || key.equals("/" + defaultProfileKey);
    }
}
