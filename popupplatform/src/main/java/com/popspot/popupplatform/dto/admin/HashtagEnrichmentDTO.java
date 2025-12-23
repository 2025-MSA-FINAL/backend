package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HashtagEnrichmentDTO {
    private Long hashId;
    private String hashName;

    private String category;
    private String summary;
    private String trendReason;

    private BigDecimal confidence;

    private String sourceType;
    private List<String> sourceUrls;

    private boolean cached;  // 캐시 사용 여부
}