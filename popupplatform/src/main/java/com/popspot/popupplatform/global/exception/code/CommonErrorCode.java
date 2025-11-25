package com.popspot.popupplatform.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    // 서버 내부에서 예상치 못한 에러가 발생했을 때 (500)
    INTERNAL_SERVER_ERROR("COMMON_500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 잘못된 요청 (400)
    INVALID_REQUEST("COMMON_400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}