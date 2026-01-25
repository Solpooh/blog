package com.solpooh.boardback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "batch_history", indexes = {
        @Index(name = "idx_job_started", columnList = "job_name, started_at DESC")
})
public class BatchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 50)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "processed_count")
    private Integer processedCount;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public enum BatchStatus {
        RUNNING,
        SUCCESS,
        FAILED
    }

    public static BatchHistoryEntity start(String jobName) {
        return BatchHistoryEntity.builder()
                .jobName(jobName)
                .status(BatchStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();
    }

    public void success(int processedCount) {
        this.status = BatchStatus.SUCCESS;
        this.finishedAt = LocalDateTime.now();
        this.processedCount = processedCount;
        this.durationMs = java.time.Duration.between(startedAt, finishedAt).toMillis();
    }

    public void fail(String errorMessage) {
        this.status = BatchStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startedAt, finishedAt).toMillis();
        this.errorMessage = errorMessage != null && errorMessage.length() > 500
                ? errorMessage.substring(0, 500)
                : errorMessage;
    }
}
