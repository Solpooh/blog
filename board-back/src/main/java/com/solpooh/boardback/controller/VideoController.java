package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.board.GetVideoListResponseDto;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video")
public class VideoController {
    private final VideoService videoService;

//    @GetMapping("")
//    public ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList() {
//        ResponseEntity<? super GetVideoListResponseDto> response = videoService.getLatestVideoList();
//        return response;
//    }
    @GetMapping("")
    public ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList(
            @PageableDefault(size = 50, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ResponseEntity<? super GetVideoListResponseDto> response = videoService.getLatestVideoList(pageable);
        return response;
    }
    @PostMapping("")
    public ResponseEntity<String> postVideo() {
        return ResponseEntity.ok(videoService.postVideo());
    }
}
