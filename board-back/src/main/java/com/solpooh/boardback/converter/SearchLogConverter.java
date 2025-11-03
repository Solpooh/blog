package com.solpooh.boardback.converter;

import com.solpooh.boardback.entity.SearchLogEntity;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SearchLogConverter {
    // 추후에 requestDTO로 묶을 것
    public static SearchLogEntity toEntity(String searchWord, String relationWord, boolean relation) {
        return SearchLogEntity.builder()
                .searchWord(searchWord)
                .relationWord(relationWord)
                .relation(relation)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }
}
