package com.solpooh.boardback.service;

import com.solpooh.boardback.entity.ChannelEntity;
import org.springframework.http.ResponseEntity;

public interface ChannelService {
    // GET: 특정 채널 정보 가져오기
    ChannelEntity getChannel(String channelId);

    // POST: 채널 정보 저장하기
    ResponseEntity<String> postChannel();
}
