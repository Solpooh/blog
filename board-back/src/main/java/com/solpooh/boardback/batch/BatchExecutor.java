package com.solpooh.boardback.batch;

import com.solpooh.boardback.entity.BatchExecutionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 핵심 파이프라인
public class BatchExecutor {
    private static final int MAX_RETRY = 3;
    private final BatchExecutionService executionService;

    public void run(BatchJobType jobType, Runnable logic) {
        BatchExecutionEntity executionEntity = executionService.start(jobType);

        try {
            logic.run();
            executionService.success(executionEntity);

        } catch (Exception e) {
            executionService.fail(executionEntity, e);

            if (!executionEntity.isRetryable(MAX_RETRY)) {
                // 알림 트리거 지정
            }
            throw e; // 로그, 모니터링을 위한 재전파
        }
    }

}
