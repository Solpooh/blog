package com.solpooh.boardback.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 영상 정렬 타입
 */
@Getter
@RequiredArgsConstructor
public enum SortType {
    LATEST("최신순"),
    VIEWS("조회수순"),
    RELEVANCE("관련도순");  // ES 스코어 기반, 검색 전용

    private final String displayName;

    public static SortType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return LATEST;
        }
        try {
            return SortType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LATEST;
        }
    }
}
