package com.popspot.popupplatform.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PopupErrorCode implements BaseErrorCode {

    // 팝업 관련 에러 코드
    THUMBNAIL_REQUIRED("POP_001", "썸네일 이미지는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("POP_002", "이미지 파일만 업로드할 수 있습니다.", HttpStatus.BAD_REQUEST),
    POPUP_NOT_FOUND("POP_003", "존재하지 않는 팝업입니다.", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE("POP_004", "종료일은 시작일보다 빠를 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}