package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.board.GetVideoListResponseDto;
import com.solpooh.boardback.entity.VideoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VideoService {
    // GET: 모든 채널의 비디오 정보 가져오기
//    ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList();
    // GET: 모든 채널의 페이징된 비디오 정보 가져오기
    ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList(Pageable pageable);

    // POST: 해당 채널의 비디오 정보 저장하기
    String postVideo();
}
