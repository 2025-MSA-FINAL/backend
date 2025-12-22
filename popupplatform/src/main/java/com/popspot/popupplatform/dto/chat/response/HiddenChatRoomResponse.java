package com.popspot.popupplatform.dto.chat.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HiddenChatRoomResponse {
    private Long crhId;
    private String crhType;      // PRIVATE / GROUP
    private Long crhRoomId;
    private String nickName;
}
