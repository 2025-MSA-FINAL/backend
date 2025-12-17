package com.popspot.popupplatform.global.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * "[1,2,3]" → List<Long>
     */
    public static List<Long> parseLongArray(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(
                    json.trim(),
                    new TypeReference<List<Long>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
