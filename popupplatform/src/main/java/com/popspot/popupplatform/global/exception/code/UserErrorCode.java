package com.popspot.popupplatform.global.exception.code;

import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("USER_002", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("USER_003", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    DUPLICATE_PHONE("USER_004", "이미 사용 중인 휴대폰 번호입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("USER_005", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    UserErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getHttpStatus() { return httpStatus; }
}


