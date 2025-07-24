package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponseDto;
import com.solpooh.boardback.dto.response.youtube.GetSearchVideoListResponseDto;
import com.solpooh.boardback.dto.response.youtube.GetVideoListResponseDto;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponseDto;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video")
public class VideoController {
    private final VideoService videoService;

    @GetMapping("")
    public ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList(
            @PageableDefault(size = 52, sort = "publishedAt") Pageable pageable
    ) {
        return videoService.getLatestVideoList(pageable);
    }
    @GetMapping("/search-list/{searchWord}")
    public ResponseEntity<? super GetSearchVideoListResponseDto> getSearchVideoList(
            @PathVariable("searchWord") String searchWord,
            String type,
            @PageableDefault(size = 52) Pageable pageable
    ) {
        return videoService.getSearchVideoList(searchWord, type, pageable);
    }

    @PostMapping("")
    public ResponseEntity<? super PostVideoResponseDto> postVideo() {
        return videoService.postVideo();
    }
    @DeleteMapping("/{videoId}")
    public ResponseEntity<? super DeleteVideoResponseDto> deleteVideo(
            @PathVariable("videoId") String videoId
    ) {
        return videoService.deleteVideo(videoId);
    }
}
