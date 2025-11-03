package com.solpooh.boardback.batch;

import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class VideoBatch {
    private final VideoService videoService;

    @Scheduled(cron = "0 0 3 * * *")
//    @Scheduled(cron = "0 0 */1 * * *")
    public void postDailyVideo() {
        log.info("▶ 비디오 배치 시작");
        PostVideoResponse response = videoService.postVideo();
        log.info("▶ 비디오 배치 완료: {}", response);
    }
}
