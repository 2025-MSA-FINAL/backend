package com.popspot.popupplatform.global.service;

import com.popspot.popupplatform.dto.global.UploadResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    UploadResultDto upload(String keyPrefix, MultipartFile file);
    void deleteByKey(String key);
}