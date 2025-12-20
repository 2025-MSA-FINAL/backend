package com.popspot.popupplatform.dto.common;

import lombok.Data;

@Data
public class PageRequestDTO {
    private int page = 0;              // 페이지 번호 (0부터 시작)
    private int size = 10;             // 페이지당 개수
    private String sortBy = "createdAt"; // 정렬 기준
    private String sortDir = "desc";   // 정렬 방향 (asc/desc)
    private String keyword = "";
    private String searchType = "all";

    // offset 계산
    public int getOffset() {
        return page * size;
    }

    // 정렬 방향이 유효한지 확인
    public String getSortDir() {
        return sortDir.equalsIgnoreCase("asc") ? "ASC" : "DESC";
    }
}
