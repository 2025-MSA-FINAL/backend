package com.popspot.popupplatform.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "사용자 제한 정보 DTO")
public class UserlimitInfoDto {
    private Long userId; //로그인userId
    private String gender; //MALE, FEMALE, NONE
    private Integer birthYear; //출생연도
}
