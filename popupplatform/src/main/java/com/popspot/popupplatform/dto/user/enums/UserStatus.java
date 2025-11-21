package com.popspot.popupplatform.dto.user.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 상태 값 (ACTIVE / PENDING / DELETED)", example = "ACTIVE")
public enum UserStatus {
    ACTIVE,
    PENDING,
    DELETED
}