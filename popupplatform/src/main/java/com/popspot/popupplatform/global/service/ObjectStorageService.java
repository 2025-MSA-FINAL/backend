package com.popspot.popupplatform.global.service;

import com.popspot.popupplatform.dto.global.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    UploadResult upload(String keyPrefix, MultipartFile file);
    void deleteByKey(String key);
}