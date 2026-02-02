package com.solpooh.boardback.service.youtube;

import com.solpooh.boardback.agent.SummaryAgent;
import com.solpooh.boardback.converter.TranscriptConverter;
import com.solpooh.boardback.dto.common.TranscriptAnalysisResult;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.TranscriptEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.event.NewVideosCollectedEvent;
import com.solpooh.boardback.fetcher.TranscriptFetcher;
import com.solpooh.boardback.repository.TranscriptRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

/**
 * Transcript 비동기 배치 처리 서비스
 *
 * - 영상 수집 이벤트 수신 후 자막 처리 (백그라운드)
 * - TranscriptService와 동일한 Lock 메커니즘 공유
 * - UNAVAILABLE 영상은 자동 skip
 * - 자막 분석과 함께 카테고리 자동 분류 수행 (95%+ 정확도)
 *
 * [카테고리 분류 전략]
 * 신규 영상: 자막(transcript) 기반 AI 분류 → 높은 정확도
 * 기존 영상: 재분류 안 함 (초기 설정 시 제목/태그/설명 기반으로 분류 완료)
 *
 * 주의: self-injection을 사용하여 같은 클래스 내 @Transactional 메서드 호출 시
 *       프록시를 통해 트랜잭션이 제대로 작동하도록 함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptAsyncService {
    private static final int MAX_AI_RETRIES = 3;
    private static final int MAX_RETRY_COUNT = 3;

    private final TranscriptRepository transcriptRepository;
    private final VideoRepository videoRepository;
    private final TranscriptFetcher transcriptFetcher;
    private final SummaryAgent summaryAgent;
    private final VideoIndexService videoIndexService;
    private final TranscriptService transcriptService;

    /**
     * Self-injection: 같은 클래스 내 트랜잭션 메서드 호출 시 프록시를 통하도록 함
     */
    @Lazy
    @Autowired
    private TranscriptAsyncService self;

    /**
     * 신규 영상 수집 이벤트 핸들러
     */
    @Async("transcriptExecutor")
    @EventListener
    public void handleNewVideosCollected(NewVideosCollectedEvent event) {
        List<String> videoIds = event.getVideoIds();
        log.info("=== Transcript 배치 처리 시작: {}개 영상 ===", videoIds.size());

        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;

        for (String videoId : videoIds) {
            try {
                ProcessResult result = processVideoTranscript(videoId);

                switch (result) {
                    case SUCCESS:
                        successCount++;
                        break;
                    case SKIPPED:
                        skipCount++;
                        break;
                    case FAILED:
                        failCount++;
                        break;
                }

            } catch (Exception e) {
                failCount++;
                log.error("Transcript 배치 처리 중 예외 발생 - videoId: {}", videoId, e);
            }
        }

        log.info("=== Transcript 배치 처리 완료: 성공 {}개, 스킵 {}개, 실패 {}개 ===",
                successCount, skipCount, failCount);
    }

    /**
     * 개별 영상 Transcript 처리
     *
     * @return ProcessResult (SUCCESS, SKIPPED, FAILED)
     */
    private ProcessResult processVideoTranscript(String videoId) {
        // 1. UNAVAILABLE 영상 체크 (자막 없는 영상은 배치에서 제외)
        if (isUnavailableVideo(videoId)) {
            log.debug("자막 불가 영상 스킵 - videoId: {}", videoId);
            return ProcessResult.SKIPPED;
        }

        // 2. Lock 획득 시도 (TranscriptService를 통해 프록시 호출)
        if (!transcriptService.acquireProcessingLock(videoId)) {
            log.debug("이미 처리 중이거나 완료된 영상 - videoId: {}", videoId);
            return ProcessResult.SKIPPED;
        }

        // 3. Lock 획득 성공 → Transcript 처리 실행
        log.info("배치 처리 시작 - videoId: {}", videoId);
        try {
            executeTranscriptProcessing(videoId);
            log.info("배치 처리 완료 - videoId: {}", videoId);
            return ProcessResult.SUCCESS;

        } catch (Exception e) {
            log.error("배치 처리 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            // self를 통해 프록시 호출 → 트랜잭션 적용
            self.handleBatchProcessingFailure(videoId, e.getMessage());
            return ProcessResult.FAILED;
        }
    }

    /**
     * Transcript 처리 실행 (yt-dlp + AI 요약/분류 + DB/ES 저장)
     */
    private void executeTranscriptProcessing(String videoId) throws Exception {
        // 1. yt-dlp로 자막 추출
        String rawTranscript = fetchTranscriptWithRetry(videoId);
        if (rawTranscript == null || rawTranscript.isBlank()) {
            throw new IllegalStateException("자막 추출 실패: 자막이 없거나 비어있음");
        }
        log.debug("자막 추출 완료 - videoId: {}, length: {}", videoId, rawTranscript.length());

        // 2. AI 요약 + 카테고리 분류 (최대 3회 재시도)
        TranscriptAnalysisResult result = summarizeAndCategorizeWithRetry(rawTranscript);
        if (result == null || result.summary() == null || result.summary().isBlank()) {
            throw new IllegalStateException("AI 분석 실패: 재시도 횟수 초과");
        }
        log.debug("AI 분석 완료 - videoId: {}, mainCategory: {}, subCategory: {}",
                videoId, result.mainCategory(), result.subCategory());

        // 3. DB 저장 (Transcript + 카테고리)
        transcriptService.completeTranscript(videoId, result.summary());
        updateVideoCategory(videoId, result);

        // 4. Elasticsearch 업데이트 (실패 시 무시)
        updateElasticsearch(videoId, result);
    }

    /**
     * 배치 처리 실패 시 상태 업데이트
     * retry_count를 1 증가시키고, MAX_RETRY_COUNT 도달 시 UNAVAILABLE로 변경
     *
     * 주의: 반드시 self를 통해 호출해야 트랜잭션이 적용됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBatchProcessingFailure(String videoId, String errorMessage) {
        int updatedRows = transcriptRepository.incrementRetryCountAndMarkFailed(videoId, errorMessage);
        log.debug("배치 실패 처리 업데이트 - videoId: {}, updatedRows: {}", videoId, updatedRows);

        if (updatedRows > 0) {
            // 재시도 횟수 확인 후 UNAVAILABLE 전환 여부 결정
            transcriptRepository.findById(videoId).ifPresent(entity -> {
                int retryCount = entity.getRetryCount();

                if (retryCount >= MAX_RETRY_COUNT) {
                    // 재시도 횟수 초과 → UNAVAILABLE로 영구 변경
                    entity.setStatus(TranscriptEntity.TranscriptStatus.UNAVAILABLE);
                    entity.setErrorMessage("재시도 횟수 초과: 자막을 제공하지 않는 영상입니다");
                    transcriptRepository.save(entity);
                    log.warn("배치 처리 재시도 한계 도달 → UNAVAILABLE 전환 - videoId: {}, retryCount: {}",
                            videoId, retryCount);
                } else {
                    log.warn("배치 처리 실패 (재시도 가능) - videoId: {}, retryCount: {}/{}",
                            videoId, retryCount, MAX_RETRY_COUNT);
                }
            });
        }
    }

    // ==================== Helper Methods ====================

    /**
     * UNAVAILABLE 상태 영상 확인
     */
    private boolean isUnavailableVideo(String videoId) {
        return transcriptRepository.findById(videoId)
                .map(entity -> entity.getStatus() == TranscriptEntity.TranscriptStatus.UNAVAILABLE)
                .orElse(false);
    }

    /**
     * yt-dlp로 자막 추출 (재시도 없음, 배치는 1회만 시도)
     */
    private String fetchTranscriptWithRetry(String videoId) {
        try {
            Path path = transcriptFetcher.fetchTranscriptJson(videoId);
            return TranscriptConverter.parseTranscript(path);
        } catch (Exception e) {
            log.debug("자막 추출 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            return null;
        }
    }

    /**
     * AI 요약 + 카테고리 분류 (최대 3회 재시도, 점진적 대기)
     */
    private TranscriptAnalysisResult summarizeAndCategorizeWithRetry(String rawTranscript) {
        for (int attempt = 1; attempt <= MAX_AI_RETRIES; attempt++) {
            try {
                return summaryAgent.summarizeAndCategorize(rawTranscript);

            } catch (Exception e) {
                log.warn("AI 분석 실패 - 시도: {}/{}, error: {}",
                        attempt, MAX_AI_RETRIES, e.getMessage());

                if (attempt < MAX_AI_RETRIES) {
                    sleep(1000L * attempt); // 1초, 2초, 3초 대기
                }
            }
        }
        return null;
    }

    /**
     * VideoEntity 카테고리 업데이트
     */
    private void updateVideoCategory(String videoId, TranscriptAnalysisResult result) {
        try {
            VideoEntity video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("영상을 찾을 수 없음: " + videoId));

            video.setMainCategory(result.mainCategory());
            video.setSubCategory(result.subCategory());
            videoRepository.save(video);

            log.info("Video 카테고리 업데이트 완료 - videoId: {}, mainCategory: {}, subCategory: {}",
                    videoId, result.mainCategory(), result.subCategory());

        } catch (Exception e) {
            log.error("Video 카테고리 업데이트 실패 - videoId: {}", videoId, e);
            // 카테고리 업데이트 실패해도 Transcript는 이미 저장되었으므로 무시
        }
    }

    /**
     * Elasticsearch transcript + category 필드 업데이트 (실패 시 무시)
     */
    private void updateElasticsearch(String videoId, TranscriptAnalysisResult result) {
        try {
            // Transcript 업데이트
            videoIndexService.updateTranscriptField(videoId, result.summary());

            // Category 업데이트
            videoIndexService.updateCategoryFields(
                    videoId,
                    result.mainCategory().name(),
                    result.subCategory().name()
            );

            log.debug("ES 업데이트 완료 - videoId: {}", videoId);
        } catch (Exception e) {
            log.warn("ES 업데이트 실패 (무시) - videoId: {}, error: {}", videoId, e.getMessage());
            // ES 실패는 DB가 이미 저장되었으므로 무시
        }
    }

    /**
     * 스레드 sleep (InterruptedException 처리)
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("스레드 인터럽트 발생");
        }
    }

    /**
     * 처리 결과 타입
     */
    private enum ProcessResult {
        SUCCESS,  // 처리 성공
        SKIPPED,  // 스킵 (UNAVAILABLE 또는 이미 처리됨)
        FAILED    // 처리 실패
    }
}
