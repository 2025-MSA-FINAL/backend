package com.popspot.popupplatform.domain.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashtagEnriched {
    private Long heId;
    private Long hashId;

    private String heCategory;
    private String heSummary;
    private String heTrendReason;

    private BigDecimal heConfidence;

    private String heSourceType;
    private String heSourceUrls;  // JSON string

    private LocalDateTime heLastAnalyzedAt;
    private LocalDateTime heExpiresAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
