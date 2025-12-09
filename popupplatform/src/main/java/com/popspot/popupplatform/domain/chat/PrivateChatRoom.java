package com.popspot.popupplatform.domain.chat;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChatRoom {
    private Long pcrId; //1:1채팅방ID
    private Long userId; //1:1채팅방 유저1
    private Long userId2; //1:1채팅방 유저2
    private Boolean pcrIsDeleted; //1:1채팅방삭제여부

    private LocalDateTime createdAt; //1:1채팅방생성일
    private LocalDateTime updatedAt; //1:1채팅방수정일
}
