package com.solpooh.boardback.dto.common;

import lombok.Builder;

@Builder
public record VideoListResponse(
        String videoId,
        String title,
        String description,
        String thumbnail,
        String publishedAt,
        String channelTitle,
        String customUrl,
        String channelThumbnail,
        Long prevViewCount,
        Long viewCount,
        Long likeCount,
        Long commentCount,
        Double trendScore
) { }
