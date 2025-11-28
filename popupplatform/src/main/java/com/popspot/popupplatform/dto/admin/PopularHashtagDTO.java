package com.popspot.popupplatform.dto.admin;

import lombok.Data;

// 인기 해시태그 DTO
@Data
public class PopularHashtagDTO {
    private Long hashId;
    private String hashName;
    private Long wishlistCount;   // 찜 개수
    private int rank;             // 순위
}
