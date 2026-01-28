package com.solpooh.boardback.dto.common;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record ChannelResponse(
        String channelId,
        String title,
        String thumbnail,
        String customUrl,
        String lang,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
