package com.popspot.popupplatform.dto.chat.request;

import com.popspot.popupplatform.dto.chat.enums.ReportType;
import lombok.Data;

import java.util.List;

@Data
public class ChatReportRequest {
    private Long repId; //생성된 신고ID
    private Long userId; //신고한사람 ID
    private ReportType reportType; //신고 유형 -- CHAT / USER
    private Long targetId; //신고 대상 ID -- 그룹 채팅: gcrId -- 개인 신고: userId
    private Long categoryId; //신고 카테고리 ID
    private List<String> imageUrls; //증거 이미지 URL 리스트
}
