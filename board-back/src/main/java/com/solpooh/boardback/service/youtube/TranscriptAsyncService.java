package com.solpooh.boardback.service.youtube;

import com.solpooh.boardback.agent.SummaryAgent;
import com.solpooh.boardback.converter.TranscriptConverter;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.TranscriptEntity;
import com.solpooh.boardback.event.NewVideosCollectedEvent;
import com.solpooh.boardback.fetcher.TranscriptFetcher;
import com.solpooh.boardback.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Transcript 비동기 처리 서비스 (동시성 제어 적용)
 * - @EventListener + @Async로 이벤트 수신
 * - TranscriptService와 동일한 Lock 메커니즘 공유
 * - 개별 영상 실패 격리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptAsyncService {
    private static final int MAX_AI_RETRIES = 3;

    private final TranscriptRepository transcriptRepository;
    private final TranscriptFetcher transcriptFetcher;
    private final SummaryAgent summaryAgent;
    private final VideoIndexService videoIndexService;
    private final TranscriptService transcriptService;

    @Async("transcriptExecutor")
    @EventListener
    public void handleNewVideosCollected(NewVideosCollectedEvent event) {
        List<String> videoIds = event.getVideoIds();
        log.info("Transcript 배치 처리 시작: {}개 영상", videoIds.size());

        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;

        for (String videoId : videoIds) {
            try {
                boolean processed = processTranscriptIfNeeded(videoId);
                if (processed) {
                    successCount++;
                } else {
                    skipCount++;
                }

            } catch (Exception e) {
                failCount++;
                log.warn("Transcript 처리 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            }
        }

        log.info("Transcript 배치 처리 완료: 성공 {}개, 스킵 {}개, 실패 {}개",
                successCount, skipCount, failCount);
    }

    /**
     * Lock 획득 후 처리 (동시성 제어 적용)
     * 1. Lock 획득 시도 (DB 레벨 CAS)
     * 2. Lock 실패 시 skip (다른 요청이 처리 중)
     * 3. Lock 성공 시 처리 실행
     */
    private boolean processTranscriptIfNeeded(String videoId) {
        // 1. Lock 획득 시도 (TranscriptService와 동일한 메커니즘)
        boolean acquired = transcriptService.tryAcquireLock(videoId);

        if (!acquired) {
            log.debug("이미 처리 중 또는 완료됨 - videoId: {}", videoId);
            return false; // 스킵
        }

        // 2. Lock 획득 성공 → 처리 시작
        log.info("Transcript 배치 처리 시작 - videoId: {}", videoId);
        try {
            processTranscript(videoId);
            return true;

        } catch (Exception e) {
            log.error("Transcript 처리 실패 - videoId: {}, error: {}", videoId, e.getMessage(), e);
            transcriptService.markAsFailed(videoId, e.getMessage());
            throw e;
        }
    }

    /**
     * 개별 영상 Transcript 처리
     * 1. yt-dlp 실행 (트랜잭션 외부)
     * 2. AI 요약 (트랜잭션 외부, 재시도 포함)
     * 3. DB 저장 (COMPLETED 상태)
     * 4. ES 필드 업데이트
     */
    private void processTranscript(String videoId) {
        // 1. yt-dlp로 자막 수집
        String rawTranscript = fetchRawTranscript(videoId);
        if (rawTranscript == null || rawTranscript.isBlank()) {
            log.debug("자막 없음 - videoId: {}", videoId);
            throw new IllegalStateException("No transcript available");
        }

        log.debug("yt-dlp 완료 - videoId: {}, length: {}", videoId, rawTranscript.length());

        // 2. AI 요약 (최대 3회 재시도)
        String summarizedTranscript = summarizeWithRetry(rawTranscript);
        if (summarizedTranscript == null || summarizedTranscript.isBlank()) {
            log.warn("AI 요약 실패 - videoId: {}", videoId);
            throw new IllegalStateException("AI summarization failed");
        }

        log.debug("AI 요약 완료 - videoId: {}, length: {}", videoId, summarizedTranscript.length());

        // 3. DB 저장 (COMPLETED 상태)
        saveTranscript(videoId, summarizedTranscript);

        // 4. ES 필드 업데이트
        try {
            videoIndexService.updateTranscriptField(videoId, summarizedTranscript);
            log.debug("ES 업데이트 완료 - videoId: {}", videoId);
        } catch (Exception e) {
            log.warn("ES 업데이트 실패 (무시) - videoId: {}, error: {}", videoId, e.getMessage());
            // ES 실패는 무시 (DB는 이미 저장됨)
        }

        log.info("Transcript 배치 처리 완료 - videoId: {}", videoId);
    }

    /**
     * yt-dlp로 자막 수집
     */
    private String fetchRawTranscript(String videoId) {
        try {
            Path path = transcriptFetcher.fetchTranscriptJson(videoId);
            return TranscriptConverter.parseTranscript(path);
        } catch (Exception e) {
            log.debug("yt-dlp 실행 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            return null;
        }
    }

    /**
     * AI 요약 (최대 3회 재시도)
     */
    private String summarizeWithRetry(String rawTranscript) {
        for (int attempt = 1; attempt <= MAX_AI_RETRIES; attempt++) {
            try {
                return summaryAgent.summarize(rawTranscript);
            } catch (Exception e) {
                log.warn("AI 요약 시도 {}/{} 실패: {}", attempt, MAX_AI_RETRIES, e.getMessage());
                if (attempt < MAX_AI_RETRIES) {
                    try {
                        Thread.sleep(1000L * attempt); // 점진적 대기
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Transcript DB 저장 (COMPLETED 상태)
     */
    @Transactional
    public void saveTranscript(String videoId, String summarizedTranscript) {
        TranscriptEntity entity = transcriptRepository.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("Lock acquired but entity not found"));

        entity.setSummarizedTranscript(summarizedTranscript);
        entity.setStatus(TranscriptEntity.TranscriptStatus.COMPLETED);
        entity.setCompletedAt(LocalDateTime.now());

        transcriptRepository.save(entity);

        log.info("Transcript DB 저장 완료 - videoId: {}", videoId);
    }
}
