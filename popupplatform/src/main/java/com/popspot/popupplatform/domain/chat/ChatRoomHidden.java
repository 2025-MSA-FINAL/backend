package com.popspot.popupplatform.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomHidden {
    private Long crhId; //채팅방숨김ID
    private String crhType; //숨김설정한 RoomType ("PRIVATE" or "GROUP")
    private Long crhRoomId; //숨김 설정한 RoomId (pcr_id or gcr_id)
    private Long userId; //숨김 설정한 유저ID
    private Boolean crhIsHidden; //채팅방숨김여부
    private LocalDateTime createdAt; //채팅방생성일
    private LocalDateTime updatedAt; //채팅방수정일
}
