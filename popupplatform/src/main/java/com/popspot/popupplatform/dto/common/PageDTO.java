package com.popspot.popupplatform.dto.common;

import lombok.Data;
import java.util.List;

@Data
public class PageDTO<T> {
    private List<T> content;           // 실제 데이터
    private int pageNumber;            // 현재 페이지 (0부터 시작)
    private int pageSize;              // 페이지당 데이터 개수
    private long totalElements;        // 전체 데이터 개수
    private int totalPages;            // 전체 페이지 수
    private boolean first;             // 첫 페이지 여부
    private boolean last;              // 마지막 페이지 여부
    private boolean empty;             // 데이터 없음 여부

    public PageDTO(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.first = pageNumber == 0;
        this.last = pageNumber >= totalPages - 1;
        this.empty = content.isEmpty();
    }
}
