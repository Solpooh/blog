package com.solpooh.boardback.batch;

import com.solpooh.boardback.cache.CacheService;
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
    private final CacheService cacheService;

    // 1시간 주기 - 비디오 저장 + 저장한 비디오 메타데이터 저장(10-개)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void postDailyVideo() {
        log.info("▶ 비디오 수집 시작");
        PostVideoResponse response = videoService.postVideo();
        log.info("▶ 비디오 수집 완료: {}", response);
        videoService.postAllVideoInfo();
        log.info("▶ 비디오 조회수 갱신 완료");

        cacheService.syncFromDB();
    }

    // 10분 주기 - Score 기반 비디오 메타데이터 저장(200+개)

    // 24시간 주기 - 비디오 Score 계산
    public void calculateScore() {
        videoService.dailyCalculate();
    }
}
