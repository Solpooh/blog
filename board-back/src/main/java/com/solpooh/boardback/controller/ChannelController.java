package com.solpooh.boardback.controller;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.response.ResponseDto;
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
    public ResponseDto<GetChannelResponse> getChannel(
            @PathVariable("channelId") String channelId
    ) {
        GetChannelResponse response = channelService.getChannel(channelId);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }
    @PostMapping("")
    public ResponseDto<PostChannelResponse> postChannel() {
        channelService.postChannel();
        return ResponseDto.of(ResponseApi.SUCCESS);
    }
}
