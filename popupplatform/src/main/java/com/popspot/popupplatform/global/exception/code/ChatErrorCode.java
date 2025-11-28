package com.popspot.popupplatform.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ChatErrorCode implements BaseErrorCode {
    ALREADY_JOINED("CHAT_001", "이미 참여 중인 채팅방입니다.", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND("CHAT_002", "존재하지 않는 채팅방입니다.", HttpStatus.NOT_FOUND),
    ROOM_FULL("CHAT_003", "채팅방이 가득 찼습니다.", HttpStatus.BAD_REQUEST),
    NOT_ROOM_OWNER("CHAT_004", "채팅방을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ROOM_ALREADY_DELETED("CHAT_005", "이미 삭제된 채팅방입니다.", HttpStatus.BAD_REQUEST),
    MAX_USER_UNDERFLOW("CHAT_006", "현재 인원보다 작은 최대 인원으로 수정할 수 없습니다.", HttpStatus.BAD_REQUEST),;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ChatErrorCode(String code, String message, HttpStatus httpStatus) {
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
