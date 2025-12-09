package com.popspot.popupplatform.global.exception.code;

import org.springframework.http.HttpStatus;

public enum ReservationErrorCode implements BaseErrorCode {

    // 예약 설정 없음
    RESERVATION_NOT_FOUND("RES_001", "해당 팝업의 예약 설정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 잘못된 단위 시간
    INVALID_ENTRY_TIME_UNIT("RES_002", "유효하지 않은 예약 단위 시간입니다.", HttpStatus.BAD_REQUEST),

    // 잘못된 시간표 범위 (시작 >= 종료)
    INVALID_TIMETABLE_TIME_RANGE("RES_003", "시간표의 시작 시간이 종료 시간보다 늦거나 같습니다.", HttpStatus.BAD_REQUEST),

    // 중복 요일 시간표
    DUPLICATE_TIMETABLE_DAY("RES_004", "중복된 요일 시간표가 존재합니다.", HttpStatus.CONFLICT),

    // 잘못된 제외일
    INVALID_EXCLUDE_DATE("RES_005", "유효하지 않은 제외일입니다.", HttpStatus.BAD_REQUEST),

    RESERVATION_ALREADY_EXISTS(
            "RES_006",
            "이미 예약 설정이 존재하여 수정할 수 없습니다.",
            HttpStatus.CONFLICT
    ),

    // ✅ 추가: 슬롯 없음
    RESERVATION_SLOT_NOT_FOUND(
            "RES_007",
            "해당 예약 시간대를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    // ✅ 추가: 정원 초과
    RESERVATION_SLOT_FULL(
            "RES_008",
            "해당 시간대가 가득 찼습니다.",
            HttpStatus.CONFLICT
    ),
    INVALID_PEOPLE_COUNT(
            "RES_009",
                    "PEOPLE값이 유효하지 않습니다.",
            HttpStatus.CONFLICT
            ),
    ;


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ReservationErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getHttpStatus() { return httpStatus; }
}
