package com.popspot.popupplatform.dto.chat.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "그룹 채팅방 참여 요청 DTO")
public class GroupChatJoinRequest {
    //참여할 그룹채팅방 ID
    private Long gcrId;
}
