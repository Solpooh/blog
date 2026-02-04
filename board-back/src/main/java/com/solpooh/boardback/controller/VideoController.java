package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.enums.MainCategory;
import com.solpooh.boardback.enums.SortType;
import com.solpooh.boardback.enums.SubCategory;
import com.solpooh.boardback.service.VideoService;
import com.solpooh.boardback.service.youtube.TranscriptService;
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

    // ===== 메인 영상 목록 =====

    @GetMapping("")
    public GetVideoListResponse getVideoList(
            @PageableDefault(size = 52) Pageable pageable,
            @RequestParam(defaultValue = "LATEST") String sort
    ) {
        SortType sortType = SortType.fromString(sort);
        return videoService.getVideoList(pageable, sortType);
    }

    // ===== 검색 =====

    @GetMapping("/search-list/{searchWord}")
    public GetSearchVideoListResponse getSearchVideoList(
            @PathVariable String searchWord,
            @PageableDefault(size = 52) Pageable pageable,
            @RequestParam(defaultValue = "RELEVANCE") String sort
    ) {
        SortType sortType = SortType.fromString(sort);
        return videoService.getSearchVideoList(searchWord, pageable, sortType);
    }

    // ===== 특수 목록 =====

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

    // ===== Transcript =====

    @GetMapping("/transcript/{videoId}")
    public GetTranscriptResponse getTranscriptVideo(
            @PathVariable String videoId
    ) {
        return transcriptService.getTranscript(videoId);
    }

    // ===== 카테고리 =====

    @GetMapping("/category/stats")
    public GetCategoryStatsResponse getCategoryStats() {
        return videoService.getCategoryStats();
    }

    @GetMapping("/category/{mainCategory}")
    public GetVideoListResponse getVideosByMainCategory(
            @PathVariable String mainCategory,
            @PageableDefault(size = 52) Pageable pageable,
            @RequestParam(defaultValue = "LATEST") String sort
    ) {
        MainCategory category = MainCategory.valueOf(mainCategory.toUpperCase());
        SortType sortType = SortType.fromString(sort);
        return videoService.getVideosByMainCategory(category, pageable, sortType);
    }

    @GetMapping("/category/{mainCategory}/{subCategory}")
    public GetVideoListResponse getVideosBySubCategory(
            @PathVariable String mainCategory,
            @PathVariable String subCategory,
            @PageableDefault(size = 52) Pageable pageable,
            @RequestParam(defaultValue = "LATEST") String sort
    ) {
        MainCategory main = MainCategory.valueOf(mainCategory.toUpperCase());
        SubCategory sub = SubCategory.valueOf(subCategory.toUpperCase());
        SortType sortType = SortType.fromString(sort);
        return videoService.getVideosBySubCategory(main, sub, pageable, sortType);
    }

    @GetMapping("/category/{mainCategory}/search/{searchWord}")
    public GetSearchVideoListResponse searchInMainCategory(
            @PathVariable String mainCategory,
            @PathVariable String searchWord,
            @PageableDefault(size = 52) Pageable pageable,
            @RequestParam(defaultValue = "RELEVANCE") String sort
    ) {
        MainCategory category = MainCategory.valueOf(mainCategory.toUpperCase());
        SortType sortType = SortType.fromString(sort);
        return videoService.searchInMainCategory(category, searchWord, pageable, sortType);
    }

    @GetMapping("/category/{mainCategory}/{subCategory}/search/{searchWord}")
    public GetSearchVideoListResponse searchInSubCategory(
            @PathVariable String mainCategory,
            @PathVariable String subCategory,
            @PathVariable String searchWord,
            @PageableDefault(size = 52) Pageable pageable,
            @RequestParam(defaultValue = "RELEVANCE") String sort
    ) {
        MainCategory main = MainCategory.valueOf(mainCategory.toUpperCase());
        SubCategory sub = SubCategory.valueOf(subCategory.toUpperCase());
        SortType sortType = SortType.fromString(sort);
        return videoService.searchInSubCategory(main, sub, searchWord, pageable, sortType);
    }
}