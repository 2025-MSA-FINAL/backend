package com.popspot.popupplatform.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 채팅방 상세 정보 응답 DTO")
public class GroupChatRoomDetailResponse {

    @Builder.Default
    private String roomType = "GROUP";

    private Long gcrId; //그룹채팅방PK
    private Long popId; //팝업스토어PK
    private String popName; //팝업스토어이름
    private Long ownerId; //방장
    private String title; //그룹채팅방이름
    private String description; //그룹채팅방설명
    private Integer maxUserCnt; //최대인원
    private Integer currentUserCnt; //현재참여인원수
    private String limitGender; //성별제한
    private Integer minAge; //최소입장나이
    private Integer maxAge; //최대입장나이
}
