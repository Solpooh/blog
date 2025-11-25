package com.solpooh.boardback.dto.common;

import java.util.List;

public record VideoMetaData(
        Long viewCount,
        Long likeCount,
        Long commentCount,
        boolean isShort,
        List<String> tagList
) { }