package com.popspot.popupplatform.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StringOrArrayDeserializer extends StdDeserializer<List<String>> {

    public StringOrArrayDeserializer() {
        super(List.class);
    }

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        List<String> result = new ArrayList<>();

        if (node == null || node.isNull()) {
            return result;
        }

        if (node.isTextual()) {
            result.add(node.asText());
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                if (element.isTextual()) {
                    result.add(element.asText());
                }
            }
        } else {
            result.add(node.toString());
        }

        return result;
    }
}
