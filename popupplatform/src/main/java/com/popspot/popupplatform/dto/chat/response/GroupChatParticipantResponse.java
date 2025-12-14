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
@Schema(description = "그룹 채팅방 참여자 정보 DTO")
public class GroupChatParticipantResponse {
    private Long userId;
    private String userName;
    private String nickName;
    private String photoUrl;
    private Long lastReadMessageId;
}
