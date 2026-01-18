package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponse;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.service.youtube.YoutubeBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 관리자용 API 호출 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/video")
public class VideoAdminController {
    private final YoutubeBatchService youtubeBatchService;
    private final VideoIndexService videoIndexService;
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
        youtubeBatchService.updateVideoData();

        return ResponseEntity.ok("updateVideoByScore completed");
    }

    @PostMapping("/calculate")
    public ResponseEntity<String> calculateScore() {
        youtubeBatchService.updateVideoScore();

        return ResponseEntity.ok("dailyCalculate completed");
    }

    @PostMapping("/index")
    public ResponseEntity<String> indexing() {
        videoIndexService.indexAll();
        return ResponseEntity.ok("ES indexing completed");
    }

}
