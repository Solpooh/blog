package com.solpooh.boardback.batch;

import com.solpooh.boardback.entity.BatchExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchExecutionRepository extends JpaRepository<BatchExecutionEntity, Long> {
    Optional<BatchExecutionEntity> findByJobTypeAndStatus(
            BatchJobType jobType,
            BatchStatus status
    );

    default Optional<BatchExecutionEntity> findRunning(BatchJobType jobType) {
        return findByJobTypeAndStatus(jobType, BatchStatus.RUNNING);
    }
    Optional<BatchExecutionEntity> findTopByJobTypeOrderByStartedAtDesc(
            BatchJobType jobType
    );
}
