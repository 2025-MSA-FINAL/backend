package com.popspot.popupplatform.global.exception.code;

import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements BaseErrorCode {
    INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("AUTH_002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("AUTH_003", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    INACTIVE_USER("AUTH_004", "로그인할 수 없는 사용자 상태입니다.", HttpStatus.FORBIDDEN),

    // 🔥 여기에 추가된 에러코드들 (기존 코드 수정 없음)
    NO_AUTH_TOKEN("AUTH_005", "인증 토큰이 제공되지 않았습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH_006", "해당 요청에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNKNOWN_AUTH_ERROR("AUTH_999","시큐리티 필터 알 수 없는 에러",HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
