package com.popspot.popupplatform.dto.chat.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatMessageListResponse {
    private List<ChatMessageResponse> messages;
    private Long lastReadMessageId;
    private List<GroupChatParticipantResponse> participants;
}

