package com.solpooh.boardback.test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class JsonParsingTest {
    // 테스트용 DTO 클래스 정의
    static class BlockDTO {
        public List<Block> blocks;
        public List<Block> getBlocks() {
            return blocks;
        }
    }

    // 샘플 데이터 가져오는 메서드
    public static String loadSampleJson(String path) throws IOException {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new FileNotFoundException("Sample file not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Heap 메모리 측정 도구
    private static long getUsedMemoryMB() {
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ie) { ie.printStackTrace();}
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // 샘플 json - Draft.js 구조와 유사하게 설계
        String jsonSample = loadSampleJson("sample/rich_text_sample.json");

        List<String> contents = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            contents.add(jsonSample);
        }
        System.out.println("▶ DTO 매핑 방식 테스트 시작");
        runObjectMapperTest(contents, objectMapper);

        System.out.println("\n▶ JsonNode 방식 테스트 시작");
        runJsonNodeTest(contents, objectMapper);

        System.out.println("\n▶ JsonParser 방식 테스트 시작");
        runStreamingParserTest(contents, new ObjectMapper());
    }

    // 1. DTO 매핑 방식
    private static void runObjectMapperTest(List<String> contents, ObjectMapper mapper) throws Exception {
        long memoryBefore = getUsedMemoryMB();
        long timeStart = System.currentTimeMillis();

        List<String> texts = new ArrayList<>();

        try {
            for (String content : contents) {
                BlockDTO blocks = mapper.readValue(content, BlockDTO.class);
                for (Block block : blocks.getBlocks()) {
                    texts.add(block.text);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        long timeEnd = System.currentTimeMillis();
        long memoryAfter = getUsedMemoryMB();

        System.out.println("⏱ 처리 시간: " + (timeEnd - timeStart) + " ms");
        System.out.println("📦 메모리 사용량: " + (memoryAfter - memoryBefore) + " MB");
        System.out.println("🔢 추출된 text 수: " + texts.size());
    }

    // 2. JsonNode 방식
    private static void runJsonNodeTest(List<String> contents, ObjectMapper mapper) throws Exception {
        long memoryBefore = getUsedMemoryMB();
        long timeStart = System.currentTimeMillis();

        List<String> texts = new ArrayList<>();
        try {
            for (String content : contents) {
                JsonNode root = mapper.readTree(content);
                JsonNode blocks = root.get("blocks");
                if (blocks != null && blocks.isArray()) {
                    for (JsonNode block : blocks) {
                        texts.add(block.get("text").asText());
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        long timeEnd = System.currentTimeMillis();
        long memoryAfter = getUsedMemoryMB();

        System.out.println("⏱ 처리 시간: " + (timeEnd - timeStart) + " ms");
        System.out.println("📦 메모리 사용량: " + (memoryAfter - memoryBefore) + " MB");
        System.out.println("🔢 추출된 text 수: " + texts.size());
    }

    private static void runStreamingParserTest(List<String> contents, ObjectMapper mapper) throws Exception {
        long memoryBefore = getUsedMemoryMB();
        long timeStart = System.currentTimeMillis();

        List<String> texts = new ArrayList<>();
        JsonFactory factory = mapper.getFactory();

        for (String content : contents) {
            JsonParser parser = factory.createParser(content);

            boolean insideBlock = false;
            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();

                if (token == JsonToken.FIELD_NAME && "blocks".equals(parser.currentName())) {
                    parser.nextToken();
                    insideBlock = true;
                }

                if (insideBlock && token == JsonToken.FIELD_NAME && "text".equals(parser.currentName())) {
                    parser.nextToken();
                    texts.add(parser.getText());
                }

                // 종료 조건: block 배열이 끝나면 false로
                if (token == JsonToken.END_ARRAY && insideBlock) {
                    insideBlock = false;
                }
            }

            parser.close();
        }

        long timeEnd = System.currentTimeMillis();
        long memoryAfter = getUsedMemoryMB();

        System.out.println("⏱ 처리 시간: " + (timeEnd - timeStart) + " ms");
        System.out.println("📦 메모리 사용량: " + (memoryAfter - memoryBefore) + " MB");
        System.out.println("🔢 추출된 text 수: " + texts.size());
    }

}
