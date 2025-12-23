package com.popspot.popupplatform.service.admin;

import com.popspot.popupplatform.dto.admin.HashtagEnrichmentDTO;
import com.popspot.popupplatform.dto.admin.TopHashtagDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface HashtagEnrichmentService {

    /**
     * TOP N 해시태그 분석 (캐시 우선, 만료 시 AI 웹 검색)
     */
    List<HashtagEnrichmentDTO> enrichTopHashtags(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int topN
    );
}
