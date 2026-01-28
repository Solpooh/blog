package com.solpooh.boardback.dto.response.youtube;

public record DeleteChannelResponse(
        String channelId,
        int deletedVideoCount
) {}
