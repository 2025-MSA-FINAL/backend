package com.popspot.popupplatform.mapper.postgres;

import com.popspot.popupplatform.dto.chat.ChatAiDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatAiDocumentMapper {
    void insertDocument(ChatAiDocument doc);
    List<ChatAiDocument> searchSimilar(
            @Param("embedding") float[] embedding,
            @Param("limit") int limit,
            @Param("type") String type
    );
    void deleteByPopupId(@Param("popupId") Long popupId);
}
