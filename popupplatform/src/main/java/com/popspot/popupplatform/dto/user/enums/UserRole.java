package com.popspot.popupplatform.dto.user.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 권한 (USER / MANAGER / ADMIN)", example = "USER")
public enum UserRole {
    USER,
    MANAGER,
    ADMIN
}
