package com.solpooh.boardback.controller;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponse;
import com.solpooh.boardback.dto.response.youtube.GetSearchVideoListResponse;
import com.solpooh.boardback.dto.response.youtube.GetVideoListResponse;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
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
    public ResponseDto<GetVideoListResponse> getLatestVideoList(
            @PageableDefault(size = 52, sort = "publishedAt") Pageable pageable
    ) {
        GetVideoListResponse response = videoService.getLatestVideoList(pageable);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }
    @GetMapping("/search-list/{searchWord}")
    public ResponseDto<GetSearchVideoListResponse> getSearchVideoList(
            @PathVariable("searchWord") String searchWord,
            String type,
            @PageableDefault(size = 52) Pageable pageable
    ) {
        GetSearchVideoListResponse response = videoService.getSearchVideoList(searchWord, type, pageable);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @PostMapping("")
    public ResponseDto<PostVideoResponse> postVideo() {
        videoService.postVideo();
        return ResponseDto.of(ResponseApi.SUCCESS);
    }
    @DeleteMapping("/{videoId}")
    public ResponseDto<DeleteVideoResponse> deleteVideo(
            @PathVariable("videoId") String videoId
    ) {
        videoService.deleteVideo(videoId);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }
}
