package com.popspot.popupplatform.service.chat.ai;

import com.popspot.popupplatform.dto.chat.ChatAiDocument;
import com.popspot.popupplatform.mapper.postgres.ChatAiDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatAiRagService {

    private final ChatAiDocumentMapper documentMapper;
    private final AiEmbeddingService embeddingService;

    /** 질문과 가장 유사한 문서 N개 검색 */
    public List<ChatAiDocument> searchRelevantDocs(String question, int limit) {
        float[] embedding = embeddingService.embed(question);
        return documentMapper.searchSimilar(embedding, limit, "popup");
    }

    /** AI 프롬프트에 넣을 Context 문자열 생성 */
    public String buildContext(String question) {
        List<ChatAiDocument> docs = searchRelevantDocs(question, 3);

        if (docs.isEmpty()) return "";

        return docs.stream()
                .map(ChatAiDocument::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
