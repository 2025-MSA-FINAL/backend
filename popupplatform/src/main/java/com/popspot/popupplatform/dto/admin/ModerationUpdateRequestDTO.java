package com.popspot.popupplatform.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data              // Getter, Setter, ToString 등 자동 생성
@NoArgsConstructor // JSON 파싱을 위한 기본 생성자
public class ModerationUpdateRequestDTO {

    /**
     * 상태 변경 요청 값
     * null  = 대기 (PENDING)
     * true  = 승인 (APPROVED)
     * false = 반려 (REJECTED)
     */
    private Boolean status;

    /**
     * 반려 사유
     * - status가 false(반려)일 때만 유효
     * - 승인/대기 일 때는 서비스에서 무시됨
     */
    private String reason;
}