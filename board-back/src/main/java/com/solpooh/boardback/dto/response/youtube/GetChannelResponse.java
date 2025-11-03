package com.solpooh.boardback.dto.response.youtube;

public record GetChannelResponse(
        String channelId,
        String title,
        String thumbnail
) {}
