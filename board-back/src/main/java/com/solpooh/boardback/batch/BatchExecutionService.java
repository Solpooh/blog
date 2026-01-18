package com.solpooh.boardback.batch;

import com.solpooh.boardback.entity.BatchExecutionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
// History 전담
public class BatchExecutionService {
    private final BatchExecutionRepository repository;

    @Transactional
    public BatchExecutionEntity start(BatchJobType jobType) {
        repository.findRunning(jobType)
                .ifPresent(e -> {
                    throw new IllegalStateException("이미 실행중인 작업입니다.");
                });
        BatchExecutionEntity entity = new BatchExecutionEntity();
        entity.setJobType(jobType);

        return repository.save(entity);
    }

    @Transactional
    public void success(BatchExecutionEntity entity) {
        entity.markSuccess();
    }
    @Transactional
    public void fail(BatchExecutionEntity entity, Throwable e) {
        entity.markFailed(e);
    }
}
