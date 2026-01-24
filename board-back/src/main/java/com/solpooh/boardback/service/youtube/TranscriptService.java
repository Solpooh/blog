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

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * Transcript 조회 및 처리
     * 1. 기존 상태 확인 (COMPLETED / PROCESSING / FAILED)
     * 2. Lock 획득 시도 (DB 레벨 CAS)
     * 3. 처리 실행 또는 적절한 응답 반환
     */
    @Transactional
    public GetTranscriptResponse getTranscript(String videoId) {
        // 1. 기존 Transcript 상태 확인
        Optional<TranscriptEntity> existedTranscript = transcriptRepository.findById(videoId);

        if (existedTranscript.isPresent()) {
            TranscriptEntity entity = existedTranscript.get();

            switch (entity.getStatus()) {
                case COMPLETED:
                    log.debug("Transcript 캐시 히트 - videoId: {}", videoId);
                    return new GetTranscriptResponse(entity.getSummarizedTranscript());

                case PROCESSING:
                    log.info("Transcript 이미 처리 중 - videoId: {}", videoId);
                    throw new CustomException(ResponseApi.TRANSCRIPT_PROCESSING);

                case FAILED:
                    return handleFailedTranscript(entity);
            }
        }

        // 2. Lock 획득 시도 (DB 레벨 CAS)
        boolean acquired = tryAcquireLockSafely(videoId);

        if (!acquired) {
            // Race condition: 다른 요청이 방금 Lock을 획득함
            log.info("Transcript 처리 중 (동시 요청) - videoId: {}", videoId);
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
     * FAILED 상태 Transcript 처리
     * - 재시도 횟수 확인 후 재처리 또는 에러 반환
     */
    private GetTranscriptResponse handleFailedTranscript(TranscriptEntity entity) {
        String videoId = entity.getVideoId();

        // 재시도 횟수 초과 확인
        if (entity.getRetryCount() >= MAX_RETRY_COUNT) {
            log.warn("Transcript 재시도 횟수 초과 - videoId: {}, retryCount: {}",
                    videoId, entity.getRetryCount());
            throw new CustomException(ResponseApi.TRANSCRIPT_RETRY_EXHAUSTED);
        }

        // 재시도 가능 → FAILED 상태 삭제 후 재처리
        log.info("Transcript 재처리 시도 - videoId: {}, retryCount: {}/{}",
                videoId, entity.getRetryCount() + 1, MAX_RETRY_COUNT);

        boolean acquired = resetAndAcquireLock(videoId, entity.getRetryCount());

        if (!acquired) {
            // 다른 요청이 먼저 재시도 시작
            throw new CustomException(ResponseApi.TRANSCRIPT_PROCESSING);
        }

        try {
            return processAndSaveTranscript(videoId);
        } catch (Exception e) {
            log.error("Transcript 재처리 실패 - videoId: {}, error: {}", videoId, e.getMessage(), e);
            markAsFailed(videoId, e.getMessage());
            throw new CustomException(ResponseApi.TRANSCRIPT_FAILED);
        }
    }

    /**
     * DB 레벨 CAS로 Lock 획득 (예외 안전)
     */
    private boolean tryAcquireLockSafely(String videoId) {
        try {
            return tryAcquireLock(videoId);
        } catch (CustomException e) {
            throw e; // NOT_EXISTED_VIDEO 등 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            log.error("Lock 획득 중 예외 발생 - videoId: {}, error: {}", videoId, e.getMessage(), e);
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
     * FAILED 상태 → PROCESSING으로 변경하며 Lock 획득 (재시도용)
     * CAS로 동시 재시도 방지
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean resetAndAcquireLock(String videoId, int expectedRetryCount) {
        int affected = transcriptRepository.resetFailedToProcessing(videoId, expectedRetryCount);
        boolean acquired = affected > 0;

        if (acquired) {
            log.debug("FAILED → PROCESSING 전환 성공 - videoId: {}", videoId);
        } else {
            log.debug("FAILED → PROCESSING 전환 실패 (동시 요청) - videoId: {}", videoId);
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
