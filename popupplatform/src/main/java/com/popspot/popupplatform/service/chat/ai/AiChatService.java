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
   자동 분기 (⭐ 핵심)
   =============================== */
    public AiAnswerMode decideAnswerMode(String userText, String context) {

        boolean isRecommend =
                userText.contains("추천")
                        || userText.contains("어디 갈")
                        || userText.contains("뭐가 좋")
                        || userText.contains("인기")
                        || userText.contains("골라");

        if (context == null || context.isBlank()) {
            return AiAnswerMode.NEED_CONFIRM;
        }

        return AiAnswerMode.RAG;
    }

    /* ===============================
       일반 안내 답변 (Q&A)
       =============================== */
    public String getAiReplyWithContext(String userText, String context) {

        String prompt = """
        [공식 정보]
        %s

        질문:
        %s

        규칙:
        - 위 정보만 사용
        - 없으면 모른다고 답해
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
        아래는 현재 팝스팟에 등록된 팝업스토어 정보야.

        [팝업 정보 시작]
        %s
        [팝업 정보 끝]

        사용자 질문:
        "%s"

        위 정보만 사용해서 팝업을 추천해줘.

        규칙:
        - 최대 3개까지만 추천
        - 아래 형식을 정확히 지켜

        형식:
        1️⃣ 팝업 이름
        - 한 줄 요약:
        - 운영 기간:
        - 장소:
        - 추천 대상:

        - 정보에 없는 내용은 절대 추가하지 마
        - 과장하지 말고 실제 안내 직원처럼 말해
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