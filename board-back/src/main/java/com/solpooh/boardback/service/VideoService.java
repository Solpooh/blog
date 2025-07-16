package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponseDto;
import com.solpooh.boardback.dto.response.youtube.GetVideoListResponseDto;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface VideoService {
    // GET: 모든 채널의 페이징된 비디오 정보 가져오기
    ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList(Pageable pageable);
    // POST: 모든 채널의 비디오 정보 저장하기
    ResponseEntity<? super PostVideoResponseDto> postVideo();
    // DELETE: 해당 채널의 비디오 정보 삭제하기
    ResponseEntity<? super DeleteVideoResponseDto> deleteVideo(String videoId);
}
