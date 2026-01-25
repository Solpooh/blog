package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.BatchHistoryEntity;
import com.solpooh.boardback.entity.BatchHistoryEntity.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchHistoryRepository extends JpaRepository<BatchHistoryEntity, Long> {

    // 특정 작업의 최근 이력 조회
    List<BatchHistoryEntity> findTop20ByJobNameOrderByStartedAtDesc(String jobName);

    // 모든 작업의 최근 이력 조회
    List<BatchHistoryEntity> findTop50ByOrderByStartedAtDesc();

    // 현재 실행 중인 작업 확인 (중복 실행 방지용)
    Optional<BatchHistoryEntity> findByJobNameAndStatus(String jobName, BatchStatus status);

    // 특정 기간의 이력 조회
    List<BatchHistoryEntity> findByStartedAtAfterOrderByStartedAtDesc(LocalDateTime since);

    // 오래된 이력 삭제
    @Modifying
    @Query("DELETE FROM BatchHistoryEntity b WHERE b.startedAt < :cutoffDate")
    int deleteByStartedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 작업별 성공률 통계
    @Query("""
            SELECT b.jobName,
                   COUNT(b),
                   SUM(CASE WHEN b.status = 'SUCCESS' THEN 1 ELSE 0 END),
                   AVG(b.durationMs)
            FROM BatchHistoryEntity b
            WHERE b.startedAt >= :since
            GROUP BY b.jobName
            """)
    List<Object[]> getStatsSince(@Param("since") LocalDateTime since);
}
