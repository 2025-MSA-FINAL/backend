package com.popspot.popupplatform.dto.chat.enums;

public enum AiAnswerMode {
    RAG,           // 내부 정보 기반
    RAG_RECOMMEND,
    PURE_LLM,      // 일반 LLM
    NEED_CONFIRM   // RAG 실패 → 사용자 선택 필요
}