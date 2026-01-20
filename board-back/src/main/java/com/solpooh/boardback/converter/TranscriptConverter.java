package com.solpooh.boardback.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class TranscriptConverter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static String parseTranscript(Path path) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = OBJECT_MAPPER.readTree(path.toFile());

        StringBuilder sb = new StringBuilder();
        for (JsonNode event : root.get("events")) {
            JsonNode segs = event.get("segs");
            if (segs != null) {
                for (JsonNode seg : segs) {
                    sb.append(seg.get("utf8").asText());
                }
            }
        }
        return sb.toString();
    }
}
