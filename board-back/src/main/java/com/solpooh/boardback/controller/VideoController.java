package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.service.VideoService;
import com.solpooh.boardback.service.youtube.TranscriptService;
import com.solpooh.boardback.service.youtube.YoutubeBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video")
public class VideoController {
    private final VideoService videoService;
    private final TranscriptService transcriptService;

    @GetMapping("")
    public GetVideoListResponse getLatestVideoList(
            @PageableDefault(size = 52, sort = "publishedAt") Pageable pageable
    ) {
        return videoService.getLatestVideoList(pageable);
    }
    @GetMapping("/search-list/{searchWord}")
    public GetSearchVideoListResponse getSearchVideoList(
            @PathVariable String searchWord,
            @PageableDefault(size = 52) Pageable pageable
    ) {
        return videoService.getSearchVideoList(searchWord, pageable);
    }

    @GetMapping("/hot-list")
    public GetHotVideoListResponse getTopTrendVideoList() {
        return videoService.getHotVideoList();
    }
    @GetMapping("/top-list")
    public GetTopViewVideoListResponse getTopViewVideoList() {
        return videoService.getTopViewVideoList();
    }
    @GetMapping("/shorts-list")
    public GetShortsVideoListResponse getShortsVideoList() {
        return videoService.getShortsVideoList();
    }

    @GetMapping("/transcript/{videoId}")
    public GetTranscriptResponse getTranscriptVideo(
            @PathVariable String videoId
    ) {
        return transcriptService.getTranscript(videoId);
    }

}