package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.youtube.GetChannelResponseDto;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import org.springframework.http.ResponseEntity;

public interface ChannelService {
    // GET: 특정 채널 정보 가져오기
    ResponseEntity<? super GetChannelResponseDto> getChannel(String channelId);

    // POST: 채널 정보 저장하기
    ResponseEntity<? super PostChannelResponseDto> postChannel();
}
