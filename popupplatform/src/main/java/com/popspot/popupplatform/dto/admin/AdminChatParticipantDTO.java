package com.popspot.popupplatform.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChatParticipantDTO {
    private Long userId;
    private String userName;
    private String nickname;
    private LocalDateTime joinedAt;
}
