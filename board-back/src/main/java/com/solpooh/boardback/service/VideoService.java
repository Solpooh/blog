package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.youtube.*;
import org.springframework.data.domain.Pageable;

public interface VideoService {
    // GET: 모든 채널의 비디오 가져오기
    GetVideoListResponse getLatestVideoList(Pageable pageable);
    // GET: 검색 비디오 가져오기
    GetSearchVideoListResponse getSearchVideoList(String searchWord, Pageable pageable);
    // GET: 인기 급상승 비디오 가져오기
    GetHotVideoListResponse getHotVideoList();
    // GET: TOP 조회수 비디오 가져오기
    GetTopViewVideoListResponse getTopViewVideoList();
    // GET: Shorts 비디오 가져오기
    GetShortsVideoListResponse getShortsVideoList();

}
