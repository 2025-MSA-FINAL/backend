package com.popspot.popupplatform.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {
    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder builder) {

        // UserReportService 방식으로 ChatClient 직접 생성
        this.chatClient = builder
                .defaultSystem("""
                    너는 팝스팟의 공식 AI 챗봇 'POPBOT'이야.
                    말투는 친절하고 자연스럽게.
                    너무 장황하게 말하지 말고 핵심 위주로 대답해줘.
                """)
                .build();
    }

    //AI응답요청 프롬프트
    public String getAiReply(String userText) {
        return chatClient.prompt()
                .user(userText)
                .call()
                .content();
    }
}
