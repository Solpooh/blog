package com.solpooh.boardback.test;

import java.util.List;
import java.util.Map;

public class Block {
    public String key;
    public String text;
    public String type;
    public int depth;
    public List<InlineStyleRange> inlineStyleRanges;
    public List<EntityRange> entityRanges;
    public Map<String, Object> data;
    public Block() {
    }
    public String getText() {
        return text;
    }
    // === 내부 클래스 정의 ===
    static class InlineStyleRange {
        public int offset;
        public int length;
        public String style;
    }

    static class EntityRange {
        public int offset;
        public int length;
        public int key;
    }
}
