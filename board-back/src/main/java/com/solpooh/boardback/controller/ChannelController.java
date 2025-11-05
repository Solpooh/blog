package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.GetChannelResponse;
import com.solpooh.boardback.dto.response.youtube.PostChannelResponse;
import com.solpooh.boardback.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channel")
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping("/{channelId}")
    public GetChannelResponse getChannel(
            @PathVariable("channelId") String channelId
    ) {
        return channelService.getChannel(channelId);
    }
    @PostMapping("")
    public PostChannelResponse postChannel() {
        return channelService.postChannel();
    }
}