package com.popspot.popupplatform.dto.report;

import lombok.Data;
import java.util.List;

@Data
public class ReportDetailDTO {
    private Long repId;
    private String repType;
    private Long repTargetId;
    private String repStatus;
    private String categoryName;
    private Long userId;
    private String userNickname;
    private String userEmail;
    private String createdAt;

    private List<String> reportImages;     // REPORT_IMAGE URL 목록
}
