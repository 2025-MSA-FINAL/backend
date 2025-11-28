package com.popspot.popupplatform.dto.report;

import lombok.Data;

@Data
public class ReportListDTO {
    private Long repId;
    private String repType;
    private String categoryName;
    private String userNickname;
    private String createdAt;
    private String repStatus;
}