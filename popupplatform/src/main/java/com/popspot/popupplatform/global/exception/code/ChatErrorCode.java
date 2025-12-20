package com.popspot.popupplatform.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ChatErrorCode implements BaseErrorCode {
    ALREADY_JOINED("CHAT_001", "이미 참여 중인 채팅방입니다.", HttpStatus.BAD_REQUEST),
    ROOM_NOT_FOUND("CHAT_002", "존재하지 않는 채팅방입니다.", HttpStatus.NOT_FOUND),
    ROOM_FULL("CHAT_003", "채팅방이 가득 찼습니다.", HttpStatus.BAD_REQUEST),
    NOT_ROOM_OWNER("CHAT_004", "채팅방을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ROOM_ALREADY_DELETED("CHAT_005", "이미 삭제된 채팅방입니다.", HttpStatus.BAD_REQUEST),
    MAX_USER_UNDERFLOW("CHAT_006", "현재 인원보다 작은 최대 인원으로 수정할 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_JOINED_ROOM("CHAT_007", "참여 중이 아닌 채팅방입니다.", HttpStatus.BAD_REQUEST),
    OWNER_CANNOT_LEAVE("CHAT_008", "방장은 채팅방을 나갈 수 없습니다.", HttpStatus.FORBIDDEN),
    MIN_USER_COUNT_INVALID("CHAT_009", "그룹 채팅방은 최소 3명 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    PRIVATE_ROOM_NOT_FOUND("CHAT_010", "1:1 채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    HIDDEN_RECORD_NOT_FOUND("CHAT_011", "숨김 기록이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    PRIVATE_ROOM_ALREADY_DELETED("CHAT_012", "이미 삭제된 1:1 채팅방입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("CHAT_013", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GENDER_NOT_ALLOWED("CHAT_014", "성별 제한에 맞지 않습니다.", HttpStatus.FORBIDDEN),
    AGE_NOT_ALLOWED("CHAT_015", "나이 제한에 맞지 않습니다.", HttpStatus.FORBIDDEN),
    INVALID_REPORT_TYPE("CHAT_016", "유효하지 않은 신고 유형입니다.", HttpStatus.BAD_REQUEST),
    INVALID_REPORT_TARGET("CHAT_017", "유효하지 않은 신고 대상입니다.", HttpStatus.BAD_REQUEST),
    CHAT_ROOM_NOT_FOUND("CHAT_018", "신고 대상 채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REPORT_SELF_NOT_ALLOWED("CHAT_019", "본인을 신고할 수 없습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_REPORT("CHAT_020", "이미 신고한 대상입니다.", HttpStatus.CONFLICT),
    IMAGE_REQUIRED("CHAT_021", "신고 이미지가 필수입니다.", HttpStatus.BAD_REQUEST);

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
