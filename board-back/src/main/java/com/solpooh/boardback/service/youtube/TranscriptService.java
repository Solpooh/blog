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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Transcript 서비스
 *
 * 동시성 제어: DB 레벨 CAS로 중복 처리 방지
 * 재시도 제한: 3회 실패 시 UNAVAILABLE 상태로 영구 변경
 *
 * 주의: self-injection을 사용하여 같은 클래스 내 @Transactional 메서드 호출 시
 *       프록시를 통해 REQUIRES_NEW가 제대로 작동하도록 함
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
     * Self-injection: 같은 클래스 내 트랜잭션 메서드 호출 시 프록시를 통하도록 함
     */
    @Lazy
    @Autowired
    private TranscriptService self;

    /**
     * Transcript 조회
     *
     * 상태별 처리:
     * - COMPLETED: 즉시 반환
     * - PROCESSING: 폴링 유도 (202 ACCEPTED)
     * - UNAVAILABLE: 자막 없음 (404 NOT FOUND)
     * - FAILED: 재시도 가능 여부 확인 후 처리
     * - 없음: 신규 처리 시작
     */
    @Transactional(readOnly = true)
    public GetTranscriptResponse getTranscript(String videoId) {
        return transcriptRepository.findById(videoId)
                .map(entity -> handleExistingTranscript(videoId, entity))
                .orElseGet(() -> startNewTranscript(videoId));
    }

    /**
     * 기존 Transcript 상태별 처리
     */
    private GetTranscriptResponse handleExistingTranscript(String videoId, TranscriptEntity entity) {
        switch (entity.getStatus()) {
            case COMPLETED:
                log.debug("Transcript 캐시 히트 - videoId: {}", videoId);
                return new GetTranscriptResponse(entity.getSummarizedTranscript());

            case PROCESSING:
                log.info("Transcript 처리 중 - videoId: {}", videoId);
                throw new CustomException(ResponseApi.TRANSCRIPT_PROCESSING);

            case UNAVAILABLE:
                log.info("Transcript 자막 없음 - videoId: {}", videoId);
                throw new CustomException(ResponseApi.TRANSCRIPT_UNAVAILABLE);

            case FAILED:
                return retryFailedTranscript(videoId, entity.getRetryCount());

            default:
                throw new IllegalStateException("Unknown transcript status: " + entity.getStatus());
        }
    }

    /**
     * 신규 Transcript 처리 시작
     */
    private GetTranscriptResponse startNewTranscript(String videoId) {
        // self를 통해 프록시 호출 → REQUIRES_NEW 트랜잭션 적용
        if (!self.acquireProcessingLock(videoId)) {
            log.info("Transcript 동시 요청 감지 - videoId: {}", videoId);
            throw new CustomException(ResponseApi.TRANSCRIPT_PROCESSING);
        }

        log.info("Transcript 신규 처리 시작 - videoId: {}", videoId);
        return executeTranscriptProcessing(videoId);
    }

    /**
     * 실패한 Transcript 재시도
     */
    private GetTranscriptResponse retryFailedTranscript(String videoId, int currentRetryCount) {
        // 재시도 횟수 초과 확인
        if (currentRetryCount >= MAX_RETRY_COUNT) {
            log.warn("Transcript 재시도 횟수 초과 - videoId: {}, retryCount: {}",
                    videoId, currentRetryCount);
            // self를 통해 프록시 호출 → REQUIRES_NEW 트랜잭션 적용
            self.markAsUnavailablePermanently(videoId);
            throw new CustomException(ResponseApi.TRANSCRIPT_UNAVAILABLE);
        }

        // self를 통해 프록시 호출 → REQUIRES_NEW 트랜잭션 적용
        if (!self.acquireRetryLock(videoId, currentRetryCount)) {
            log.info("Transcript 동시 재시도 요청 감지 - videoId: {}", videoId);
            throw new CustomException(ResponseApi.TRANSCRIPT_PROCESSING);
        }

        log.info("Transcript 재시도 시작 - videoId: {}, 시도: {}/{}",
                videoId, currentRetryCount + 1, MAX_RETRY_COUNT);
        return executeTranscriptProcessing(videoId);
    }

    /**
     * Transcript 처리 실행 (yt-dlp + AI 요약 + DB 저장)
     */
    private GetTranscriptResponse executeTranscriptProcessing(String videoId) {
        try {
            // 1. yt-dlp로 자막 추출
            Path transcriptPath = transcriptFetcher.fetchTranscriptJson(videoId);
            String rawTranscript = TranscriptConverter.parseTranscript(transcriptPath);
            log.debug("자막 추출 완료 - videoId: {}, length: {}", videoId, rawTranscript.length());

            // 2. AI 요약
            String summarized = summaryAgent.summarize(rawTranscript);
            log.debug("AI 요약 완료 - videoId: {}, length: {}", videoId, summarized.length());

            // 3. COMPLETED 상태로 저장 (self를 통해 프록시 호출)
            self.completeTranscript(videoId, summarized);
            log.info("Transcript 처리 완료 - videoId: {}", videoId);

            return new GetTranscriptResponse(summarized);

        } catch (Exception e) {
            log.error("Transcript 처리 실패 - videoId: {}, error: {}", videoId, e.getMessage());
            // self를 통해 프록시 호출 → REQUIRES_NEW 트랜잭션 적용 (별도 커밋)
            self.handleProcessingFailure(videoId, e.getMessage());
            throw new CustomException(ResponseApi.TRANSCRIPT_FAILED);
        }
    }

    /**
     * 처리 실패 시 상태 업데이트
     * retry_count를 1 증가시키고, MAX_RETRY_COUNT 도달 시 UNAVAILABLE로 변경
     *
     * 주의: 반드시 self를 통해 호출해야 REQUIRES_NEW가 적용됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProcessingFailure(String videoId, String errorMessage) {
        int updatedRows = transcriptRepository.incrementRetryCountAndMarkFailed(videoId, errorMessage);
        log.debug("실패 처리 업데이트 - videoId: {}, updatedRows: {}", videoId, updatedRows);

        if (updatedRows > 0) {
            // 업데이트된 retry_count 확인
            transcriptRepository.findById(videoId).ifPresent(entity -> {
                int retryCount = entity.getRetryCount();

                if (retryCount >= MAX_RETRY_COUNT) {
                    // 재시도 횟수 초과 → UNAVAILABLE로 영구 변경
                    entity.setStatus(TranscriptEntity.TranscriptStatus.UNAVAILABLE);
                    entity.setErrorMessage("재시도 횟수 초과: 자막을 제공하지 않는 영상입니다");
                    transcriptRepository.save(entity);
                    log.warn("Transcript 재시도 한계 도달 → UNAVAILABLE 전환 - videoId: {}, retryCount: {}",
                            videoId, retryCount);
                } else {
                    log.warn("Transcript 실패 (재시도 가능) - videoId: {}, retryCount: {}/{}",
                            videoId, retryCount, MAX_RETRY_COUNT);
                }
            });
        }
    }

    // ==================== Lock 관리 ====================

    /**
     * 신규 처리용 Lock 획득
     * INSERT를 통해 PROCESSING 상태로 생성 (retry_count=0)
     *
     * 주의: 반드시 self를 통해 호출해야 REQUIRES_NEW가 적용됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireProcessingLock(String videoId) {
        if (!videoRepository.existsByVideoId(videoId)) {
            throw new CustomException(ResponseApi.NOT_EXISTED_VIDEO);
        }

        int affected = transcriptRepository.tryAcquireProcessingLock(videoId);
        boolean acquired = affected > 0;

        if (acquired) {
            log.debug("Lock 획득 성공 - videoId: {}", videoId);
        } else {
            log.debug("Lock 획득 실패 (이미 존재) - videoId: {}", videoId);
        }

        return acquired;
    }

    /**
     * 재시도용 Lock 획득
     * FAILED → PROCESSING 전환 및 retry_count 증가 (CAS)
     *
     * 주의: 반드시 self를 통해 호출해야 REQUIRES_NEW가 적용됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireRetryLock(String videoId, int expectedRetryCount) {
        int affected = transcriptRepository.resetFailedToProcessing(videoId, expectedRetryCount);
        boolean acquired = affected > 0;

        if (acquired) {
            log.debug("재시도 Lock 획득 - videoId: {}, retryCount: {} → {}",
                    videoId, expectedRetryCount, expectedRetryCount + 1);
        } else {
            log.debug("재시도 Lock 실패 (동시 요청) - videoId: {}", videoId);
        }

        return acquired;
    }

    // ==================== 상태 변경 ====================

    /**
     * COMPLETED 상태로 변경
     *
     * 주의: 반드시 self를 통해 호출해야 REQUIRES_NEW가 적용됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeTranscript(String videoId, String summarizedTranscript) {
        transcriptRepository.findById(videoId).ifPresent(entity -> {
            entity.setSummarizedTranscript(summarizedTranscript);
            entity.setStatus(TranscriptEntity.TranscriptStatus.COMPLETED);
            entity.setCompletedAt(LocalDateTime.now());
            entity.setErrorMessage(null);
            transcriptRepository.save(entity);
        });
    }

    /**
     * UNAVAILABLE 상태로 영구 변경 (자막 제공 불가)
     *
     * 주의: 반드시 self를 통해 호출해야 REQUIRES_NEW가 적용됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsUnavailablePermanently(String videoId) {
        transcriptRepository.findById(videoId).ifPresent(entity -> {
            entity.setStatus(TranscriptEntity.TranscriptStatus.UNAVAILABLE);
            entity.setErrorMessage("재시도 횟수 초과: 자막을 제공하지 않는 영상입니다");
            transcriptRepository.save(entity);

            log.warn("Transcript UNAVAILABLE 상태로 영구 변경 - videoId: {}", videoId);
        });
    }
}
