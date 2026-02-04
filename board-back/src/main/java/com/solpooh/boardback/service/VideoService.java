package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.enums.MainCategory;
import com.solpooh.boardback.enums.SortType;
import com.solpooh.boardback.enums.SubCategory;
import org.springframework.data.domain.Pageable;

public interface VideoService {
    // GET: 모든 채널의 비디오 가져오기
    GetVideoListResponse getLatestVideoList(Pageable pageable);
    // GET: 모든 채널의 비디오 가져오기 (정렬 옵션)
    GetVideoListResponse getVideoList(Pageable pageable, SortType sortType);
    // GET: 검색 비디오 가져오기
    GetSearchVideoListResponse getSearchVideoList(String searchWord, Pageable pageable);
    // GET: 검색 비디오 가져오기 (정렬 옵션)
    GetSearchVideoListResponse getSearchVideoList(String searchWord, Pageable pageable, SortType sortType);
    // GET: 인기 급상승 비디오 가져오기
    GetHotVideoListResponse getHotVideoList();
    // GET: TOP 조회수 비디오 가져오기
    GetTopViewVideoListResponse getTopViewVideoList();
    // GET: Shorts 비디오 가져오기
    GetShortsVideoListResponse getShortsVideoList();

    // 카테고리 통계
    GetCategoryStatsResponse getCategoryStats();
    // 대분류 카테고리 조회
    GetVideoListResponse getVideosByMainCategory(MainCategory mainCategory, Pageable pageable, SortType sortType);
    // 소분류 카테고리 조회
    GetVideoListResponse getVideosBySubCategory(MainCategory mainCategory, SubCategory subCategory, Pageable pageable, SortType sortType);
    // 대분류 내 검색
    GetSearchVideoListResponse searchInMainCategory(MainCategory mainCategory, String searchWord, Pageable pageable, SortType sortType);
    // 소분류 내 검색
    GetSearchVideoListResponse searchInSubCategory(MainCategory mainCategory, SubCategory subCategory, String searchWord, Pageable pageable, SortType sortType);

    // Admin: 전체 비디오 목록
    GetAdminVideoListResponse getAdminVideoList(Pageable pageable);
    // Admin: 채널명 검색
    GetAdminVideoListResponse searchAdminVideosByChannel(String channelTitle, Pageable pageable);
}
