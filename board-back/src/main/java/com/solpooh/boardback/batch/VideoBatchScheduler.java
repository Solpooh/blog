package com.solpooh.boardback.batch;

import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.service.youtube.YoutubeBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class VideoBatchScheduler {
    private final YoutubeBatchService youtubeBatchService;
    private final CacheService cacheService;
    private final BatchExecutor batchExecutor;

    // 1시간 주기 - 비디오 저장 + 저장한 비디오 메타데이터 저장(10-개)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void postHourlyVideo() {
//        PostVideoResponse response = youtubeBatchService.postVideo();
        batchExecutor.run(
                BatchJobType.VIDEO_COLLECT_HOURLY,
                youtubeBatchService::postVideo
        );
        cacheService.syncFromDB();
    }

    // 10분 주기 - Score 기반 비디오 메타데이터 저장(200+개)
    @Scheduled(cron = "0 */10 * * * *")
    public void postMinutelyVideoData() {
        batchExecutor.run(
                BatchJobType.VIDEO_DATA_UPDATE,
                youtubeBatchService::updateVideoData
        );
    }

    // 24시간 주기 - 비디오 Score 계산
    @Scheduled(cron = "0 0 3 * * *")
    public void postDailyCalculate() {
        batchExecutor.run(
                BatchJobType.VIDEO_SCORE_UPDATE,
                youtubeBatchService::updateVideoScore
        );
    }
}
