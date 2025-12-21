package com.popspot.popupplatform.service.chat.ai;

import com.popspot.popupplatform.dto.chat.enums.AiAnswerMode;
import com.popspot.popupplatform.dto.global.UploadResultDto;
import com.popspot.popupplatform.global.service.ObjectStorageService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class AiChatService {
    private final ChatClient ragClient;
    private final ChatClient pureClient;
    private final ImageModel imageModel;
    private final ObjectStorageService storage;

    public AiChatService(ChatClient.Builder builder,
                ImageModel imageModel,
                ObjectStorageService storage) {
// ChatClient 직접 생성
            this.ragClient = builder
                    .defaultSystem("""
                    너는 팝스팟(Popspot)의 공식 AI 챗봇 'POPBOT'이야.
                    반드시 제공된 Context 정보만 사용해.
                    정보가 없으면 모른다고 말해.
                """)
                    .build();

            this.pureClient = builder
                    .defaultSystem("""
                    너는 일반 AI 챗봇이야.
                    팝스팟 공식 정보가 아닐 수 있어.
                    불확실한 정보는 그럴 수 있다고 안내해.
                """)
                    .build();

            this.imageModel = imageModel;
            this.storage = storage;
    }

    //AI응답요청 프롬프트
    public String getPureLlmReply(String userText) {
        return pureClient.prompt()
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

    /* ===============================
       일반 안내 답변 (Q&A)
       =============================== */
    public String getAiReplyWithContext(String userText, String context) {

        String prompt = """
    너는 팝스팟(Popspot)의 공식 안내 AI 'POPBOT'이야.
    너의 역할은 '정보를 전달하는 직원'처럼 친절하게 안내하는 거야.

    =====================
    [사용 가능한 공식 정보]
    %s
    =====================

    [사용자 질문]
    %s

    답변 규칙:
    1. 반드시 위 공식 정보 안에서만 답변해.
    2. 정보가 정확히 일치하지 않으면:
       - "없습니다"로 끝내지 말고
       - 왜 없는지 간단히 설명해
       - 사용자가 다음에 할 수 있는 선택지를 제안해
    3. 문장은 자연스럽고 대화체로 작성해.
    4. 목록이 있으면 보기 좋게 정리해.
    5. 과장하거나 추측하지 마.

    답변 스타일:
    - 친절한 안내 직원
    - 차분하지만 도움이 되게
    - 너무 짧지 않게 (2~4문장 권장)

    예시 톤:
    "현재 팝스팟에 등록된 정보 기준으로 안내드릴게요 🙂"
    """.formatted(context, userText);

        return ragClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /* ===============================
       NEED_CONFIRM JSON 응답
       =============================== */
    public String needConfirmResponse() {
        return """
        {
          "type": "NEED_CONFIRM",
          "message": "현재 팝스팟 정보만으로는 정확한 답변이 어려워요.",
          "actions": [
            {
              "label": "일반 AI로 질문하기",
              "action": "PURE_LLM"
            }
          ]
        }
        """;
    }

    /* ===============================
       추천 전용 답변
       =============================== */
    public String getAiRecommendReply(String userText, String context) {

        if (context == null || context.isBlank()) {
            return """
        아직 추천할 수 있는 팝업 정보가 충분하지 않아요.
        승인된 팝업이 더 등록되면 추천해 드릴게요 🙂
        """;
        }

        String prompt = """
            너는 팝스팟(Popspot)의 공식 추천 AI 'POPBOT'이야.
            아래 정보와 사용자 조건을 바탕으로 팝업을 추천해줘.
        
            =====================
            [현재 팝스팟에 등록된 팝업 정보]
            %s
            =====================
        
            [사용자 조건]
            %s
            
                ⚠️ 출력은 반드시 JSON으로만 해.
                    ⚠️ 설명 문장, 인삿말 절대 금지.
                
                    출력 형식:
                    {
                      "type": "POPUP_RECOMMEND",
                      "items": [
                        {
                          "popId": number,
                          "popName": string,
                          "popThumbnail": string,
                          "popLocation": string,
                          "reason": string
                        }
                      ]
                    }
        
            추천 규칙:
            1. 반드시 위 팝업 정보 안에서만 추천해.
            2. 사용자 조건(지역, 대상, 분위기 등)을 최대한 반영해.
            3. 조건이 정확히 일치하지 않으면,
               - 가장 가까운 팝업을 추천하고
               - 왜 추천했는지 이유를 설명해.
            4. 최대 3개까지만 추천해.
            5. 정보에 없는 내용은 절대 추가하지 마.
        
            톤:
            - 실제 안내 직원처럼
            - 과장 없이 친절하게
            """.formatted(context, userText);

        return ragClient.prompt()
                .user(prompt)
                .call()
                .content();
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