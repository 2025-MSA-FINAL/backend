package com.popspot.popupplatform.dto.chat;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "챗봇AI 문서 DTO")
public class ChatAiDocument {
    private Long id;
    private String content;
    private float[] embedding;
    private String metadata;
    private LocalDateTime createdAt;
}
