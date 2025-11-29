package com.popspot.popupplatform.dto.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "그룹 채팅방 생성 요청 DTO")
public class CreateGroupChatRoomRequest {
    private Long popId; //팝업스토어ID
    private String title; //그룹채팅방이름
    private String description; //그룹채팅방설명
    private Integer maxUserCnt; //그룹채팅방최대인원
    private String limitGender; //그룹채팅방성별제한
    private Integer minAge; //그룹채팅방최소나이
    private Integer maxAge; //그룹채팅방최대나이
}
