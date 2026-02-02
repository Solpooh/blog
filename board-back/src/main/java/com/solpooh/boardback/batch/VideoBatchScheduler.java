package com.solpooh.boardback.batch;

import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.entity.BatchHistoryEntity;
import com.solpooh.boardback.service.BatchHistoryService;
import com.solpooh.boardback.service.youtube.YoutubeBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoBatchScheduler {

    private final YoutubeBatchService youtubeBatchService;
    private final CacheService cacheService;
    private final BatchHistoryService batchHistoryService;

    // 1시간 주기 - 비디오 저장 + 캐시 동기화
    @Scheduled(cron = "0 0 * * * *")
    public void postHourlyVideo() {
        String jobName = "VIDEO_COLLECT";
        log.info("[BATCH:{}] 시작", jobName);
        BatchHistoryEntity history = batchHistoryService.start(jobName);

        try {
            PostVideoResponse response = youtubeBatchService.postVideo();
            cacheService.syncFromDB();

            int savedCount = (response != null && response.videoIds() != null) ? response.videoIds().size() : 0;
            batchHistoryService.success(history, savedCount);
            log.info("[BATCH:{}] 완료 - 처리: {}건, 소요: {}ms", jobName, savedCount, history.getDurationMs());

        } catch (Exception e) {
            batchHistoryService.fail(history, e);
            log.error("[BATCH:{}] 실패: {}", jobName, e.getMessage(), e);
        }
    }

    // 3시간 주기 - Score 기반 비디오 메타데이터 갱신
    @Scheduled(cron = "0 0 */3 * * *")
    public void postMinutelyVideoData() {
        String jobName = "VIDEO_DATA_UPDATE";
        log.info("[BATCH:{}] 시작", jobName);
        BatchHistoryEntity history = batchHistoryService.start(jobName);

        try {
            int updatedCount = youtubeBatchService.updateVideoData();

            batchHistoryService.success(history, updatedCount);
            log.info("[BATCH:{}] 완료 - 처리: {}건, 소요: {}ms", jobName, updatedCount, history.getDurationMs());

        } catch (Exception e) {
            batchHistoryService.fail(history, e);
            log.error("[BATCH:{}] 실패: {}", jobName, e.getMessage(), e);
        }
    }

    // 1시간 주기 - 비디오 Score 계산
    @Scheduled(cron = "0 0 * * * *")
    public void postDailyCalculate() {
        String jobName = "VIDEO_SCORE_UPDATE";
        log.info("[BATCH:{}] 시작", jobName);
        BatchHistoryEntity history = batchHistoryService.start(jobName);

        try {
            int updatedCount = youtubeBatchService.updateVideoScore();

            batchHistoryService.success(history, updatedCount);
            log.info("[BATCH:{}] 완료 - 처리: {}건, 소요: {}ms", jobName, updatedCount, history.getDurationMs());

        } catch (Exception e) {
            batchHistoryService.fail(history, e);
            log.error("[BATCH:{}] 실패: {}", jobName, e.getMessage(), e);
        }
    }
}
