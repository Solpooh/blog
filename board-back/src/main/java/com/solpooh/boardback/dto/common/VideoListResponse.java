package com.solpooh.boardback.dto.common;

import com.solpooh.boardback.entity.TagEntity;

import java.util.List;

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
        Double trendScore,
        boolean isShort
//        List<TagEntity> tagList
) { }
