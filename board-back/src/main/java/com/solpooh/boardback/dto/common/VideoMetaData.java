package com.solpooh.boardback.dto.common;

import lombok.Builder;

import java.util.List;
@Builder
public record VideoMetaData(
        String videoId,
        Long prevViewCount,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Boolean isShort,
        List<String> tags,
        Double trendScore
) { }