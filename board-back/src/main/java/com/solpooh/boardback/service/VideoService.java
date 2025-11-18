package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.youtube.*;
import org.springframework.data.domain.Pageable;

public interface VideoService {
    // GET: 모든 채널의 페이징된 비디오 정보 가져오기
    GetVideoListResponse getLatestVideoList(Pageable pageable);
    // GET: 검색어로 검색된 페이징된 비디오 정보 가져오기
    GetSearchVideoListResponse getSearchVideoList(String searchWord, String type, Pageable pageable);
    // POST: 모든 채널의 비디오 정보 저장하기
    PostVideoResponse postVideo();
    // DELETE: 해당 채널의 비디오 정보 삭제하기
    DeleteVideoResponse deleteVideo(String videoId);

    void postVideoInfo();
    GetTopTrendVideoListResponse getTopTrendVideoList();
}
