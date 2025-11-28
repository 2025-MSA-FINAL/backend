package com.popspot.popupplatform.dto.report;

import lombok.Data;

@Data
public class ReportDTO {
    private Long repId;
    private String repType;
    private Long repTargetId;
    private String repStatus;
    private Long rcId;
    private Long userId;
    private String createdAt;
}
