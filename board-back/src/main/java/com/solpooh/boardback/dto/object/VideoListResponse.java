package com.solpooh.boardback.dto.object;

public record VideoListResponse(
        String videoId,
        String title,
        String thumbnail,
        String publishedAt,
        String channelTitle,
        String channelId,
        String channelThumbnail
) { }
