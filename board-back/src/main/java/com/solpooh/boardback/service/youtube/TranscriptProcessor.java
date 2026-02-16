package com.solpooh.boardback.service.youtube;

import com.solpooh.boardback.agent.SummaryAgent;
import com.solpooh.boardback.converter.TranscriptConverter;
import com.solpooh.boardback.dto.common.TranscriptAnalysisResult;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.fetcher.TranscriptFetcher;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Transcript 처리 핵심 로직
 *
 * 책임:
 * - 자막 추출 (yt-dlp)
 * - AI 분석 (요약 + 카테고리 분류)
 * - 결과 저장 (VideoEntity, Elasticsearch)
 *
 * 사용처:
 * - TranscriptService (수동 API 호출)
 * - TranscriptAsyncService (자동 배치 처리)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptProcessor {

    private final TranscriptFetcher transcriptFetcher;
    private final SummaryAgent summaryAgent;
    private final VideoRepository videoRepository;
    private final VideoIndexService videoIndexService;

    private static final int MAX_AI_RETRIES = 3;

    /**
     * Transcript 전체 처리 파이프라인
     *
     * @param videoId YouTube 영상 ID
     * @return AI 분석 결과 (요약 + 카테고리)
     * @throws Exception 자막 추출 또는 AI 분석 실패 시
     */
    public TranscriptAnalysisResult process(String videoId) throws Exception {
        // 1. 자막 추출
        String rawTranscript = extractTranscript(videoId);
        log.debug("자막 추출 완료 - videoId: {}, length: {}", videoId, rawTranscript.length());

        // 2. AI 분석 (요약 + 카테고리 분류)
        TranscriptAnalysisResult result = analyzeWithAI(rawTranscript);
        log.debug("AI 분석 완료 - videoId: {}, mainCategory: {}, subCategory: {}",
                videoId, result.mainCategory(), result.subCategory());

        // 3. 결과 저장 (VideoEntity 카테고리 + Elasticsearch)
        saveResults(videoId, result);

        return result;
    }

    /**
     * 자막 추출 (yt-dlp)
     *
     * @param videoId YouTube 영상 ID
     * @return 추출된 자막 텍스트
     * @throws Exception yt-dlp 실행 실패 시
     */
    private String extractTranscript(String videoId) throws Exception {
        Path transcriptPath = transcriptFetcher.fetchTranscriptJson(videoId);
        String rawTranscript = TranscriptConverter.parseTranscript(transcriptPath);

        if (rawTranscript == null || rawTranscript.isBlank()) {
            throw new IllegalStateException("자막 추출 실패: 자막이 없거나 비어있음");
        }

        return rawTranscript;
    }

    /**
     * AI 분석 (요약 + 카테고리 분류)
     * 최대 3회 재시도, 점진적 대기 (1초, 2초, 3초)
     *
     * @param rawTranscript 원본 자막 텍스트
     * @return AI 분석 결과
     * @throws Exception AI 분석 실패 (재시도 횟수 초과)
     */
    private TranscriptAnalysisResult analyzeWithAI(String rawTranscript) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_AI_RETRIES; attempt++) {
            try {
                TranscriptAnalysisResult result = summaryAgent.summarizeAndCategorize(rawTranscript);

                if (result == null || result.summary() == null || result.summary().isBlank()) {
                    throw new IllegalStateException("AI 분석 결과가 비어있음");
                }

                return result;

            } catch (Exception e) {
                lastException = e;
                log.warn("AI 분석 실패 - 시도: {}/{}, error: {}",
                        attempt, MAX_AI_RETRIES, e.getMessage());

                if (attempt < MAX_AI_RETRIES) {
                    sleep(1000L * attempt); // 1초, 2초, 3초 대기
                }
            }
        }

        throw new IllegalStateException("AI 분석 실패: 재시도 횟수 초과", lastException);
    }

    /**
     * 결과 저장 (VideoEntity 카테고리 + Elasticsearch)
     *
     * @param videoId YouTube 영상 ID
     * @param result AI 분석 결과
     */
    private void saveResults(String videoId, TranscriptAnalysisResult result) {
        // VideoEntity 카테고리 업데이트
        updateVideoCategory(videoId, result);

        // Elasticsearch 업데이트 (실패 시 무시)
        updateElasticsearch(videoId, result);
    }

    /**
     * VideoEntity 카테고리 업데이트
     *
     * @param videoId YouTube 영상 ID
     * @param result AI 분석 결과
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
            // 카테고리 업데이트 실패해도 Transcript는 저장되므로 무시
        }
    }

    /**
     * Elasticsearch transcript + category 필드 업데이트
     *
     * @param videoId YouTube 영상 ID
     * @param result AI 분석 결과
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
            // ES 실패는 DB가 정합성 소스이므로 무시
        }
    }

    /**
     * 스레드 sleep (InterruptedException 처리)
     *
     * @param millis 대기 시간 (밀리초)
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("스레드 인터럽트 발생");
        }
    }
}
