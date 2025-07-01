package com.solpooh.boardback.controller;

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
    public ResponseEntity<ChannelEntity> getChannel(
            @PathVariable("channelId") String channelId
    ) {
        ChannelEntity channelEntity = channelService.getChannel(channelId);
        return ResponseEntity.ok(channelEntity);
    }
    @PostMapping("")
    public ResponseEntity<String> postChannel() {
        return channelService.postChannel();
    }
}
