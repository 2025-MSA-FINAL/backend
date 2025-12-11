package com.popspot.popupplatform.dto.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "그룹 채팅방 수정 요청 DTO")
public class UpdateGroupChatRoomRequest {
    private String title; //그룹채팅방이름
    private String description; //그룹채팅방설명
    private Integer maxUserCnt; //그룹채팅방최대인원수
}
