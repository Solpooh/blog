package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponse;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.service.youtube.YoutubeBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/video")
public class VideoAdminController {
    private final YoutubeBatchService youtubeBatchService;

    @PostMapping("")
    public PostVideoResponse postVideo() {
        return youtubeBatchService.postVideo();
    }
    @DeleteMapping("/{videoId}")
    public DeleteVideoResponse deleteVideo(
            @PathVariable("videoId") String videoId
    ) {
        return youtubeBatchService.deleteVideo(videoId);
    }

    @PostMapping("/update")
    public ResponseEntity<String> refreshScoreTargets() {
        youtubeBatchService.updateVideoByScore();

        return ResponseEntity.ok("updateVideoByScore completed");
    }

    @PostMapping("/calculate")
    public ResponseEntity<String> calculateScore() {
        youtubeBatchService.dailyCalculate();

        return ResponseEntity.ok("dailyCalculate completed");
    }
}
