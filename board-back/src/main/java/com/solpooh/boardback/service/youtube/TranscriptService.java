package com.solpooh.boardback.service.youtube;

import com.solpooh.boardback.agent.SummaryAgent;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.TranscriptConverter;
import com.solpooh.boardback.dto.response.youtube.GetTranscriptResponse;
import com.solpooh.boardback.entity.TranscriptEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.fetcher.TranscriptFetcher;
import com.solpooh.boardback.repository.TranscriptRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Transcript 서비스 (동시성 제어 적용)
 * - DB 레벨 CAS(Compare-And-Set)로 중복 처리 방지
 * - TranscriptEntity 분리로 Video 테이블 복잡도 감소
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptService {
    private final TranscriptRepository transcriptRepository;
    private final VideoRepository videoRepository;
    private final TranscriptFetcher transcriptFetcher;
    private final SummaryAgent summaryAgent;

    /**
     * Transcript 조회 및 처리
     * 1. COMPLETED 상태 확인 (빠른 경로)
     * 2. Lock 획득 시도 (DB 레벨 CAS)
     * 3. 처리 실행 또는 대기
     */
    @Transactional(readOnly = true)
    public GetTranscriptResponse getTranscript(String videoId) {
        // 1. COMPLETED 상태 확인 (빠른 경로)
        Optional<TranscriptEntity> completed =
                transcriptRepository.findByVideoIdAndStatus(
                        videoId,
                        TranscriptEntity.TranscriptStatus.COMPLETED
                );

        if (completed.isPresent()) {
            log.debug("Transcript 캐시 히트 - videoId: {}", videoId);
            return new GetTranscriptResponse(completed.get().getSummarizedTranscript());
        }

        // 2. Lock 획득 시도 (DB 레벨 CAS)
        boolean acquired = tryAcquireLock(videoId);

        if (!acquired) {
            // 다른 요청이 이미 처리 중
            log.info("Transcript 처리 중 - videoId: {}", videoId);
            throw new CustomException(ResponseApi.TRANSCRIPT_PROCESSING);
        }

        // 3. Lock 획득 성공 → 처리 시작
        log.info("Transcript 처리 시작 - videoId: {}", videoId);
        try {
            return processAndSaveTranscript(videoId);
        } catch (Exception e) {
            log.error("Transcript 처리 실패 - videoId: {}, error: {}", videoId, e.getMessage(), e);
            markAsFailed(videoId, e.getMessage());
            throw new CustomException(ResponseApi.TRANSCRIPT_FAILED);
        }
    }

    /**
     * DB 레벨 CAS로 Lock 획득
     * - INSERT ON DUPLICATE KEY UPDATE로 Atomic 연산
     * - 반환값: 1 = 성공, 0 = 실패 (이미 존재)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryAcquireLock(String videoId) {
        // Video 존재 여부 확인
        if (!videoRepository.existsByVideoId(videoId)) {
            throw new CustomException(ResponseApi.NOT_EXISTED_VIDEO);
        }

        // DB 레벨 CAS 실행
        int affected = transcriptRepository.tryAcquireProcessingLock(videoId);
        boolean acquired = affected > 0;

        if (acquired) {
            log.debug("Lock 획득 성공 - videoId: {}", videoId);
        } else {
            log.debug("Lock 획득 실패 (이미 처리 중) - videoId: {}", videoId);
        }

        return acquired;
    }

    /**
     * Transcript 처리 및 저장
     * 1. yt-dlp 실행
     * 2. AI 요약
     * 3. DB 저장 (COMPLETED 상태)
     */
    private GetTranscriptResponse processAndSaveTranscript(String videoId) throws IOException, InterruptedException {
        // 1. yt-dlp 실행 → 자막 파싱
        Path path = transcriptFetcher.fetchTranscriptJson(videoId);
        String rawTranscript = TranscriptConverter.parseTranscript(path);

        log.debug("yt-dlp 완료 - videoId: {}, length: {}", videoId, rawTranscript.length());

        // 2. AI 요약
        String summarized = summaryAgent.summarize(rawTranscript);

        log.debug("AI 요약 완료 - videoId: {}, length: {}", videoId, summarized.length());

        // 3. DB 저장
        TranscriptEntity entity = transcriptRepository.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("Lock acquired but entity not found"));

        entity.setSummarizedTranscript(summarized);
        entity.setStatus(TranscriptEntity.TranscriptStatus.COMPLETED);
        entity.setCompletedAt(LocalDateTime.now());

        transcriptRepository.save(entity);

        log.info("Transcript 처리 완료 - videoId: {}", videoId);

        return new GetTranscriptResponse(summarized);
    }

    /**
     * 처리 실패 시 FAILED 상태로 변경
     */
    @Transactional
    public void markAsFailed(String videoId, String errorMessage) {
        transcriptRepository.findById(videoId).ifPresent(entity -> {
            entity.setStatus(TranscriptEntity.TranscriptStatus.FAILED);
            entity.setErrorMessage(errorMessage);
            entity.setRetryCount(entity.getRetryCount() + 1);
            transcriptRepository.save(entity);

            log.warn("Transcript FAILED 상태로 변경 - videoId: {}, retryCount: {}",
                    videoId, entity.getRetryCount());
        });
    }
}
