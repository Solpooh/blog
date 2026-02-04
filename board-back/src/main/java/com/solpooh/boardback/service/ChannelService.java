package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.channel.PostChannelRequest;
import com.solpooh.boardback.dto.response.youtube.DeleteChannelResponse;
import com.solpooh.boardback.dto.response.youtube.GetChannelListResponse;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponse;

public interface ChannelService {
    // Admin 채널 관리
    GetChannelListResponse getChannelList();
    PostChannelResponse addChannel(PostChannelRequest request);
    DeleteChannelResponse deleteChannel(String channelId);

    // 캐시된 채널 총 개수 조회
    Long getTotalChannelCount();
}
