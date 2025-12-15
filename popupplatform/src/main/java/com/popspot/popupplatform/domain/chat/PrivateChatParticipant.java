package com.popspot.popupplatform.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChatParticipant {
    private Long pcrId; //채팅방ID
    private Long userId; //로그인유저ID
    private Long lastReadMessageId; //마지막으로 읽은 메시지ID
}
