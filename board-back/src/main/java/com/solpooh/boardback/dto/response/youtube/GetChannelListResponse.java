package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.dto.common.ChannelResponse;

import java.util.List;

public record GetChannelListResponse(
        List<ChannelResponse> channelList
) {}
