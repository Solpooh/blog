package com.solpooh.boardback.dto.common;

import java.util.List;

public record VideoMetaData(
        String videoId,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Boolean isShort
) { }