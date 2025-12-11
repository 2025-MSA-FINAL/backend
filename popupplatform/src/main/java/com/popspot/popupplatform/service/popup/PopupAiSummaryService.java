package com.popspot.popupplatform.service.popup;

import com.popspot.popupplatform.mapper.popup.PopupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PopupAiSummaryService {

    // AI 클라이언트
    private final ChatClient chatClient;

    // DB 업데이트용 매퍼
    private final PopupMapper popupMapper;

    public PopupAiSummaryService(ChatClient.Builder builder,
                                 PopupMapper popupMapper) {
        this.popupMapper = popupMapper;

        // 기본 시스템 프롬프트 설정
        this.chatClient = builder
                .defaultSystem("""
                    당신은 MZ세대를 타겟팅하는 팝업스토어 마케팅 카피라이터입니다.
                    아래에 제공되는 팝업 정보를 바탕으로 요약 문구를 작성하세요.

                    출력 형식 규칙:
                    - 한국어로 작성합니다.
                    - 총 글자 수는 공백 포함 150자 이내로 작성합니다.
                    - 3줄로 출력하며, 각 줄은 한 문장으로 구성합니다.
                    - 각 줄에 1개 정도의 이모지를 자연스럽게 포함합니다.
                    - 해시태그(#)나 제목, 따옴표는 넣지 않습니다.
                    - 너무 과장된 광고 문구보다, 실제로 방문 욕구가 생기는 자연스러운 톤을 유지합니다.
                """)
                .build();
    }

    /**
     * 팝업 등록 이후, 비동기로 AI 요약 생성 + DB 업데이트
     * - @Async 덕분에 별도 쓰레드에서 실행됨
     * - 트랜잭션/HTTP 응답과 분리
     */
    @Async
    public void generateAndUpdateSummaryAsync(Long popId,
                                              String popName,
                                              String popDescription,
                                              List<String> hashtags) {
        try {
            String summary = generateSummaryInternal(popName, popDescription, hashtags);

            // DB에 요약 업데이트
            popupMapper.updatePopupAiSummary(popId, summary);

            log.info("AI 요약 업데이트 완료: popId={}, summary={}", popId, summary);
        } catch (Exception e) {
            log.error("AI 요약 비동기 생성/저장 실패: popId={}, error={}", popId, e.getMessage(), e);
            // 실패해도 서비스 전체 흐름은 깨지지 않도록 여기서만 잡고 끝냄
        }
    }

    /**
     * 실제 AI 호출 로직 (내부 전용)
     */
    private String generateSummaryInternal(String popName,
                                           String popDescription,
                                           List<String> hashtags) {

        // 해시태그 null/빈 리스트 방어
        String hashtagText = (hashtags == null || hashtags.isEmpty())
                ? "해시태그 없음"
                : String.join(", ", hashtags);

        // 사용자 요청 메시지 구성
        String userRequest = String.format(
                "팝업 이름: %s%n" +
                        "팝업 설명: %s%n" +
                        "해시태그: %s%n%n" +
                        "위 정보를 바탕으로, 방문 욕구가 생기도록 매력적인 요약글을 만들어줘.",
                popName,
                popDescription,
                hashtagText
        );

        // AI 호출
        return chatClient.prompt()
                .user(userRequest)
                .call()
                .content();
    }
}
