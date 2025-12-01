package com.popspot.popupplatform.domain.chat;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant {
    private Long gcrId; //그룹채팅방ID
    private Long userId; //참가한유저ID
    private Long cmId; //마지막읽은 메시지ID
}
