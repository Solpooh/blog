package com.solpooh.boardback.controller;

import com.solpooh.boardback.entity.Video;
import com.solpooh.boardback.service.implement.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class YoutubeController {
    private final VideoService videoService;

    @GetMapping("/{channelId}")
    public ResponseEntity<List<Video>> fetchLatestVideos(
            @PathVariable("channelId") String channelId
    ) {
        try {
            List<Video> videos = videoService.fetchAndSaveLatestVideos(channelId);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
