package com.solpooh.boardback.service.youtube;

import com.solpooh.boardback.agent.SummaryAgent;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.TranscriptConverter;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.event.NewVideosCollectedEvent;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.fetcher.TranscriptFetcher;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Phase 2: Transcript 비동기 처리
 * - @EventListener + @Async로 이벤트 수신
 * - 개별 영상 실패 격리
 * - REQUIRES_NEW 트랜잭션으로 독립 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptAsyncService {
    private static final int MAX_AI_RETRIES = 3;

    private final TranscriptFetcher transcriptFetcher;
    private final SummaryAgent summaryAgent;
    private final VideoRepository videoRepository;
    private final VideoIndexService videoIndexService;

    @Async("transcriptExecutor")
    @EventListener
    public void handleNewVideosCollected(NewVideosCollectedEvent event) {
        List<String> videoIds = event.getVideoIds();
        log.info("Transcript 처리 시작: {}개 영상", videoIds.size());

        int successCount = 0;
        int failCount = 0;

        for (String videoId : videoIds) {
            try {
                processTranscript(videoId);
                successCount++;

            } catch (Exception e) {
                failCount++;
                log.warn("Transcript 처리 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            }
        }

        log.info("Transcript 처리 완료: 성공 {}개, 실패 {}개", successCount, failCount);
    }

    /**
     * 개별 영상 Transcript 처리
     * 1. yt-dlp 실행 (트랜잭션 외부)
     * 2. AI 요약 (트랜잭션 외부)
     * 3. DB 저장 (REQUIRES_NEW 트랜잭션)
     * 4. ES 필드 업데이트
     */
    private void processTranscript(String videoId) {
        // 1. yt-dlp로 자막 수집 (트랜잭션 외부)
        String rawTranscript = fetchRawTranscript(videoId);
        if (rawTranscript == null || rawTranscript.isBlank()) {
            log.debug("자막 없음 - videoId: {}", videoId);
            return;
        }

        // 2. AI 요약 (트랜잭션 외부, 재시도 포함)
        String summarizedTranscript = summarizeWithRetry(rawTranscript);
        if (summarizedTranscript == null || summarizedTranscript.isBlank()) {
            log.warn("AI 요약 실패 - videoId: {}", videoId);
            return;
        }

        // 3. DB 저장 (REQUIRES_NEW 트랜잭션)
        saveTranscript(videoId, summarizedTranscript);

        // 4. ES 필드 업데이트
        videoIndexService.updateTranscriptField(videoId, summarizedTranscript);

        log.debug("Transcript 처리 성공 - videoId: {}", videoId);
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
     * Transcript DB 저장 (REQUIRES_NEW 트랜잭션)
     */
    public void saveTranscript(String videoId, String transcript) {
        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_VIDEO));
        videoEntity.setTranscript(transcript);

        videoRepository.save(videoEntity);

        log.info("Transcript DB 저장완료 - videoId: {}", videoId);
    }
}
