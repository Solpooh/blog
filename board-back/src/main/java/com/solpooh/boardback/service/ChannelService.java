package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.youtube.GetChannelResponse;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponse;

public interface ChannelService {
    GetChannelResponse getChannel(String channelId);
    PostChannelResponse postChannel();
}
