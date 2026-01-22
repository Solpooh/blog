package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.TranscriptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TranscriptRepository extends JpaRepository<TranscriptEntity, String> {

    /**
     * 특정 상태의 transcript 조회 (주로 COMPLETED 조회용)
     */
    Optional<TranscriptEntity> findByVideoIdAndStatus(
            String videoId,
            TranscriptEntity.TranscriptStatus status
    );

    /**
     * 동시성 제어: Atomic INSERT로 Lock 획득
     * 반환값: 1 = Lock 획득 성공, 0 = 이미 존재 (Lock 실패)
     *
     * INSERT ON DUPLICATE KEY UPDATE는 MySQL 전용 문법
     * PK 충돌 시 아무것도 하지 않고 affected rows = 0 반환
     */
    @Modifying
    @Query(value =
            "INSERT INTO transcript (video_id, status, started_at, retry_count, created_at, updated_at) " +
            "VALUES (:videoId, 'PROCESSING', NOW(), 0, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE video_id = video_id",
            nativeQuery = true)
    int tryAcquireProcessingLock(@Param("videoId") String videoId);

    /**
     * 실패 상태이면서 재시도 횟수가 maxRetry 미만인 영상 조회
     * 배치 재처리용
     */
    List<TranscriptEntity> findByStatusAndRetryCountLessThan(
            TranscriptEntity.TranscriptStatus status,
            int maxRetry
    );

    /**
     * videoId로 존재 여부 확인
     */
    boolean existsByVideoId(String videoId);
}
