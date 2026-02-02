package com.solpooh.boardback.dto.common;

import com.solpooh.boardback.enums.MainCategory;
import com.solpooh.boardback.enums.SubCategory;
import lombok.Builder;

/**
 * Transcript 분석 결과 (요약 + 카테고리)
 */
@Builder
public record TranscriptAnalysisResult(
        String summary,
        MainCategory mainCategory,
        SubCategory subCategory
) {
    public static TranscriptAnalysisResult ofSummaryOnly(String summary) {
        return TranscriptAnalysisResult.builder()
                .summary(summary)
                .mainCategory(MainCategory.ETC)
                .subCategory(SubCategory.NONE)
                .build();
    }
}
