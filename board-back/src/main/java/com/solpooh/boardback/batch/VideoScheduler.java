package com.solpooh.boardback.batch;

import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class VideoScheduler {
    private final VideoService videoService;

    // 1시간 마다 동영상 수집 + 조회수 갱신
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void postDailyVideo() {
        log.info("▶ 비디오 수집 시작");
        PostVideoResponse response = videoService.postVideo();
        log.info("▶ 비디오 수집 완료: {}", response);
        videoService.postAllVideoInfo();
        log.info("▶ 비디오 조회수 갱신 완료");
    }
}
