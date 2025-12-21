package com.popspot.popupplatform.service.chat.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popspot.popupplatform.dto.chat.ChatAiDocument;
import com.popspot.popupplatform.mapper.postgres.ChatAiDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatDocumentService {

    private final ChatAiDocumentMapper mapper;
    private final AiEmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(String content, Map<String, Object> metadata) {
        float[] embedding = embeddingService.embed(content);

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new RuntimeException("metadata JSON 변환 실패", e);
        }

        ChatAiDocument doc = ChatAiDocument.builder()
                .content(content)
                .embedding(embedding)
                .metadata(metadataJson)
                .build();

        mapper.insertDocument(doc);
    }

    public List<ChatAiDocument> search(String query, int limit, String type) {
        float[] embedding = embeddingService.embed(query);
        return mapper.searchSimilar(embedding, limit, type);
    }

    @Transactional
    public void deleteByPopupId(Long popupId) {
        mapper.deleteByPopupId(popupId);
    }
}
