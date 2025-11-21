package com.popspot.popupplatform.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "휴대폰 문자 인증 요청 DTO")
public class PhoneVerificationRequest {

    /** 휴대폰 번호 (하이픈 없이 01012345678 형식 권장) */
    private String phone;

    /** 사용자 입력 인증번호 */
    private String code;
}

