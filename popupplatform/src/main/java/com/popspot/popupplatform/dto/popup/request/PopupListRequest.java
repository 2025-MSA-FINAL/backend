package com.popspot.popupplatform.dto.popup.request;

import com.popspot.popupplatform.dto.popup.enums.PopupSortOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class PopupListRequest {

    @Schema(description = "커서 값 (서버에서 준 문자열 그대로 전달)", example = "2025-12-31T00:00:00_105")
    private String cursor;

    @Schema(description = "한 번에 가져올 개수", example = "10")
    private Integer size;

    @Schema(description = "팝업 이름 검색 키워드", example = "팝업")
    private String keyword;

    @Schema(description = "지역 필터 (여러 개 가능)", example = "[\"서울시 성동구\", \"서울시 강남구\"]")
    private List<String> regions;

    @Schema(description = "기간 필터 시작일 (겹치면 포함 규칙)", example = "2025-01-01")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "기간 필터 종료일 (겹치면 포함 규칙)", example = "2025-01-02")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "진행 상태 필터 (UPCOMING / ONGOING / CLOSED)", example = "ONGOING")
    private String status;

    @Schema(description = "최소 가격 필터", example = "0")
    private Integer minPrice;

    @Schema(description = "최대 가격 필터", example = "20000")
    private Integer maxPrice;

    @Schema(description = "정렬 옵션 (DEADLINE / CREATED / VIEW / POPULAR)", example = "DEADLINE")
    private PopupSortOption sort;

    /**
     * size가 null/이상한 값으로 들어와도 안전하게 처리하는 보조 메서드
     */
    public int getSafeSize() {
        int defaultSize = 10;
        int maxSize = 50;

        if (size == null) {
            return defaultSize;
        }
        if (size < 1) {
            return 1;
        }
        return Math.min(size, maxSize);
    }

    /**
     * 기간 필터: startDate / endDate가 둘 다 있고 startDate > endDate면 자동으로 뒤집어서 사용
     */
    public LocalDate getSafeStartDate() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return endDate;
        }
        return startDate;
    }

    public LocalDate getSafeEndDate() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return startDate;
        }
        return endDate;
    }

    /**
     * 가격 필터: minPrice / maxPrice가 둘 다 있고 min > max면 자동으로 뒤집어서 사용
     */
    public Integer getSafeMinPrice() {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return maxPrice;
        }
        return minPrice;
    }

    public Integer getSafeMaxPrice() {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return minPrice;
        }
        return maxPrice;
    }

    /**
     * 정렬 옵션 기본값 DEADLINE
     */
    public PopupSortOption getSafeSort() {
        if (sort == null) {
            return PopupSortOption.DEADLINE;
        }
        return sort;
    }
}