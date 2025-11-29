package com.popspot.popupplatform.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "그룹 채팅방 목록 응답 DTO")
public class GroupChatRoomListResponse {
    private Long gcrId; //그룹채팅방ID
    private String title; //그룹채팅방이름
    private String description; //그룹채팅방설명
    private Integer maxUserCnt; //그룹채팅방최대인원수
    private Integer currentUserCnt; //그룹채팅방현재인원수
    private String limitGender; //그룹채팅방성별제한
    private Integer minAge; //그룹채팅방최소연령
    private Integer maxAge; //그룹채팅방쵀대연령
}
