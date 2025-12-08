package com.popspot.popupplatform.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "사용자 제한 정보 DTO")
public class UserLimitInfoDto {
    private Long userId; //로그인userId
    private String userGender; //MALE, FEMALE, NONE
    private Integer userBirthyear; //출생연도
}
