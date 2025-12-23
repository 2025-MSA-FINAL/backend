package com.popspot.popupplatform.domain.admin;

import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAIReportChart {
    private Long arcId;          // 차트 고유 PK
    private Long airId;          // 어떤 리포트에 속한 차트인지 (FK)
    private String arcType;      // GENDER_RATIO, AGE_GROUP 등
    private String arcImageUrl;  // 서버의 이미지 저장 경로
    private LocalDateTime createdAt;
}
