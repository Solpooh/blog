package com.solpooh.boardback.dto.request.channel;

import jakarta.validation.constraints.NotBlank;

public record PostChannelRequest(
        @NotBlank(message = "channelId는 필수입니다")
        String channelId
) {}
