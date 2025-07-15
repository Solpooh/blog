package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.GetChannelResponseDto;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channel")
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping("/{channelId}")
    public ResponseEntity<? super GetChannelResponseDto> getChannel(
            @PathVariable("channelId") String channelId
    ) {
        return channelService.getChannel(channelId);
    }
    @PostMapping("")
    public ResponseEntity<? super PostChannelResponseDto> postChannel() {
        return channelService.postChannel();
    }
}
