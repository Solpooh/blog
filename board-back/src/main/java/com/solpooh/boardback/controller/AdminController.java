package com.solpooh.boardback.controller;

import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.dto.request.admin.DeleteVideosRequest;
import com.solpooh.boardback.dto.request.channel.PostChannelRequest;
import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.service.ChannelService;
import com.solpooh.boardback.service.VideoService;
import com.solpooh.boardback.service.youtube.YoutubeBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// 관리자용 API 호출 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
//@PreAuthorize("hasRole('ADMIN')") // 클래스 레벨 보안: 모든 메서드에 ADMIN 권한 필요
public class AdminController {
    private final YoutubeBatchService youtubeBatchService;
    private final VideoIndexService videoIndexService;
    private final CacheService cacheService;
    private final ChannelService channelService;
    private final VideoService videoService;

    // ===== 수동 API 호출 -> Video 관리 =====

    @PostMapping("/video")
    public PostVideoResponse postVideo() {
        PostVideoResponse response = youtubeBatchService.postVideo();
        cacheService.syncFromDB();  // 캐시 동기화
        return response;
    }

    @DeleteMapping("/video/{videoId}")
    public DeleteVideoResponse deleteVideo(
            @PathVariable("videoId") String videoId
    ) {
        return youtubeBatchService.deleteVideo(videoId);
    }

    @DeleteMapping("/videos")
    public DeleteVideosResponse deleteVideos(
            @Valid @RequestBody DeleteVideosRequest request
    ) {
        int deletedCount = youtubeBatchService.deleteVideos(request.videoIds());
        return new DeleteVideosResponse(deletedCount);
    }

    @PostMapping("/video/update")
    public ResponseEntity<String> refreshScoreTargets() {
        youtubeBatchService.updateVideoData();
        return ResponseEntity.ok("updateVideoByScore completed");
    }

    @PostMapping("/video/calculate")
    public ResponseEntity<String> calculateScore() {
        youtubeBatchService.updateVideoScore();
        return ResponseEntity.ok("dailyCalculate completed");
    }

    @PostMapping("/video/index")
    public ResponseEntity<String> indexing() {
        videoIndexService.indexAll();
        return ResponseEntity.ok("ES indexing completed");
    }

    @PostMapping("/video/reindex")
    public ResponseEntity<String> reindexing() {
        videoIndexService.indexAll(true);  // 인덱스 삭제 후 재생성
        return ResponseEntity.ok("ES reindexing completed (index recreated)");
    }

    // ===== 관리자 페이지 -> Channel 관리 =====

    @GetMapping("/channel")
    public GetChannelListResponse getChannelList() {
        return channelService.getChannelList();
    }

    @PostMapping("/channel")
    public PostChannelResponse addChannel(
            @Valid @RequestBody PostChannelRequest request
    ) {
        return channelService.addChannel(request);
    }

    @DeleteMapping("/channel/{channelId}")
    public DeleteChannelResponse deleteChannel(
            @PathVariable("channelId") String channelId
    ) {
        return channelService.deleteChannel(channelId);
    }

    // ===== 관리자 페이지 -> Video 관리 =====

    @GetMapping("/videos")
    public GetAdminVideoListResponse getAdminVideoList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return videoService.getAdminVideoList(pageable);
    }

    @GetMapping("/videos/search")
    public GetAdminVideoListResponse searchAdminVideosByChannel(
            @RequestParam String channelTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return videoService.searchAdminVideosByChannel(channelTitle, pageable);
    }
}
