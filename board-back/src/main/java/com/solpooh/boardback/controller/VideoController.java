package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video")
public class VideoController {
    private final VideoService videoService;

    @GetMapping("")
    public GetVideoListResponse getLatestVideoList(
            @PageableDefault(size = 52, sort = "publishedAt") Pageable pageable
    ) {
        return videoService.getLatestVideoList(pageable);
    }
    @GetMapping("/search-list/{searchWord}")
    public GetSearchVideoListResponse getSearchVideoList(
            @PathVariable("searchWord") String searchWord,
            String type,
            @PageableDefault(size = 52) Pageable pageable
    ) {
        return videoService.getSearchVideoList(searchWord, type, pageable);
    }

    @PostMapping("")
    public PostVideoResponse postVideo() {
        return videoService.postVideo();
    }
    @DeleteMapping("/{videoId}")
    public DeleteVideoResponse deleteVideo(
            @PathVariable("videoId") String videoId
    ) {
        return videoService.deleteVideo(videoId);
    }

    @PostMapping("/info")
    public void postVideoInfo() {
        videoService.postVideoInfo();
    }
    @GetMapping("/hot-list")
    public GetHotVideoListResponse getTopTrendVideoList() {
        return videoService.getHotVideoList();
    }
    @GetMapping("/top-list")
    public GetTopViewVideoListResponse getTopViewVideoList() {
        return videoService.getTopViewVideoList();
    }

}