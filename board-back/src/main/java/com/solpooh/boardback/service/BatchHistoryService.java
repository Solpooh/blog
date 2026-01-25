package com.solpooh.boardback.service;

import com.solpooh.boardback.entity.BatchHistoryEntity;
import com.solpooh.boardback.entity.BatchHistoryEntity.BatchStatus;
import com.solpooh.boardback.repository.BatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchHistoryService {

    private final BatchHistoryRepository batchHistoryRepository;

    /**
     * 배치 작업 시작 기록
     * - 새 트랜잭션으로 분리하여 배치 실패 시에도 이력 보존
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchHistoryEntity start(String jobName) {
        // 중복 실행 체크
        batchHistoryRepository.findByJobNameAndStatus(jobName, BatchStatus.RUNNING)
                .ifPresent(running -> {
                    log.warn("[BATCH:{}] 이미 실행 중인 작업이 있습니다. ID: {}", jobName, running.getId());
                });

        BatchHistoryEntity entity = BatchHistoryEntity.start(jobName);
        return batchHistoryRepository.save(entity);
    }

    /**
     * 배치 작업 성공 기록
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(BatchHistoryEntity entity, int processedCount) {
        entity.success(processedCount);
        batchHistoryRepository.save(entity);
    }

    /**
     * 배치 작업 실패 기록
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(BatchHistoryEntity entity, Exception e) {
        entity.fail(e.getMessage());
        batchHistoryRepository.save(entity);
    }

    /**
     * 특정 작업의 최근 이력 조회
     */
    @Transactional(readOnly = true)
    public List<BatchHistoryEntity> getRecentHistory(String jobName) {
        return batchHistoryRepository.findTop20ByJobNameOrderByStartedAtDesc(jobName);
    }

    /**
     * 모든 작업의 최근 이력 조회
     */
    @Transactional(readOnly = true)
    public List<BatchHistoryEntity> getAllRecentHistory() {
        return batchHistoryRepository.findTop50ByOrderByStartedAtDesc();
    }

    /**
     * 오래된 이력 삭제 (90일 이전)
     */
    @Transactional
    public int cleanupOldHistory(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deleted = batchHistoryRepository.deleteByStartedAtBefore(cutoffDate);
        log.info("[BATCH:CLEANUP] {}일 이전 이력 삭제: {}건", retentionDays, deleted);
        return deleted;
    }
}
