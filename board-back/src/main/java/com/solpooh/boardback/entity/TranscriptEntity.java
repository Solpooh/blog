package com.solpooh.boardback.entity;

import com.solpooh.boardback.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "transcript")
public class TranscriptEntity extends BaseTimeEntity {

    @Id
    @Column(name = "video_id", length = 20)
    private String videoId; // PK = 동시성 제어 키

    @Column(name = "summarized_transcript", columnDefinition = "TEXT")
    private String summarizedTranscript; // AI 요약본만 저장

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TranscriptStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public enum TranscriptStatus {
        PROCESSING,  // 처리 중
        COMPLETED,   // 완료
        FAILED,      // 실패 (재처리 가능)
        UNAVAILABLE  // 자막 없음 (영구적, 재시도 불가)
    }
}
