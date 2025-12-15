package com.popspot.popupplatform.service.chat;

import com.popspot.popupplatform.dto.global.UploadResultDto;
import com.popspot.popupplatform.global.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class AiChatService {
    private final ChatClient chatClient;
    private final ImageModel imageModel;
    private final ObjectStorageService storage;

    public AiChatService(ChatClient.Builder builder,
                         ImageModel imageModel,
                         ObjectStorageService storage) {

        // ChatClient 직접 생성
        this.chatClient = builder
                .defaultSystem("""
                    너는 팝스팟의 공식 AI 챗봇 'POPBOT'이야.
                    말투는 친절하고 자연스럽게.
                    너무 장황하게 말하지 말고 핵심 위주로 대답해줘.
                """)
                .build();
        this.imageModel = imageModel;
        this.storage = storage;
    }

    //AI응답요청 프롬프트
    public String getAiReply(String userText) {
        return chatClient.prompt()
                .user(userText)
                .call()
                .content();
    }

    public boolean isImageRequest(String text) {
        if (text == null) return false;

        String t = text.toLowerCase();

        return t.contains("그려")
                || t.contains("그림")
                || t.contains("이미지")
                || t.contains("사진")
                || t.contains("일러스트")
                || t.contains("draw")
                || t.contains("image")
                || t.contains("illustration");
    }

    /** 🖼 AI 이미지 생성 (MockMultipartFile ❌) */
    public UploadResultDto generateImage(String prompt) {
        String refinedPrompt = """
        A high-quality, clean illustration suitable for a chat application.

        Subject:
        %s

        Style:
        - modern digital illustration
        - soft lighting
        - clean background
        - no text, no watermark, no logo

        Composition:
        - centered subject
        - minimal background
        - balanced framing

        Quality:
        - high resolution
        - sharp focus
        - vivid but natural colors

        Aspect ratio:
        - 1:1
        """.formatted(prompt);

        ImageResponse response = imageModel.call(new ImagePrompt(refinedPrompt));

        String imageUrl = response.getResult().getOutput().getUrl();

        try {
            byte[] imageBytes = new URL(imageUrl).openStream().readAllBytes();

            return storage.uploadBytes(
                    "chat/ai",
                    imageBytes,
                    "image/png",
                    "png"
            );

        } catch (Exception e) {
            throw new RuntimeException("AI 이미지 생성 실패", e);
        }
    }
}