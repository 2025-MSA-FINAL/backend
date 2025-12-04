package com.popspot.popupplatform.dto.chat;

import lombok.Data;

@Data
public class UserlimitInfoDto {
    private Long userId; //로그인userId
    private String gender; //MALE, FEMALE, NONE
    private Integer birthYear; //출생연도
}
