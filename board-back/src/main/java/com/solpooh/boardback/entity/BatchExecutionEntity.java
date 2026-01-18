package com.solpooh.boardback.entity;

import com.solpooh.boardback.batch.BatchJobType;
import com.solpooh.boardback.batch.BatchStatus;
import com.solpooh.boardback.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "batch_history")
@Table(name = "batch_history")
public class BatchExecutionEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private BatchJobType jobType;
    @Enumerated(EnumType.STRING)
    private BatchStatus status;
    private int attemptCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Column(length = 1000)
    private String errorMessage;

    // 상태 전이 메서드
    public void markRunning() {
        this.status = BatchStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }
    public void markSuccess() {
        this.status = BatchStatus.SUCCESS;
        this.finishedAt = LocalDateTime.now();
    }

    public void markFailed(Throwable e) {
        this.status = BatchStatus.FAILED;
        this.attemptCount++;
        this.finishedAt = LocalDateTime.now();
        this.errorMessage = e.getMessage();
    }

    public boolean isRetryable(int maxRetry) {
        return this.attemptCount < maxRetry;
    }
}
