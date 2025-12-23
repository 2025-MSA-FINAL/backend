package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.domain.admin.HashtagEnriched;
import com.popspot.popupplatform.dto.admin.TopHashtagDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface HashtagEnrichedMapper {

    /**
     * 특정 기간 TOP N 해시태그 조회
     */
    List<TopHashtagDTO> findTopHashtagsByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit
    );

    /**
     * 해시태그 캐시 조회 (TTL 확인)
     */
    Optional<HashtagEnriched> findValidCacheByHashId(@Param("hashId") Long hashId);

    /**
     * 해시태그 분석 결과 저장
     */
    void insertEnrichment(HashtagEnriched enrichment);

    /**
     * 해시태그 분석 결과 업데이트
     */
    void updateEnrichment(HashtagEnriched enrichment);
}
