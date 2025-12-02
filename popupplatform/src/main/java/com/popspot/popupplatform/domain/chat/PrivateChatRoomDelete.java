package com.popspot.popupplatform.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChatRoomDelete {
    private Long pcrdId; //1대1채팅방삭제ID
    private Long pcrId; //삭제할1대1방ID
    private Long userId; //삭제 설정한 유저ID
    private Boolean pcrdIsDeleted; //1대1채팅방삭제여부
    private LocalDateTime lastDeletedAt; //마지막으로삭제한시점
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
