package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopHashtagDTO {
    private Long hashId;
    private String hashName;
    private Long usageCount;  // 사용 횟수
}
