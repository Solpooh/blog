package com.solpooh.boardback.repository;

import com.solpooh.boardback.dto.common.VideoMetaData;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VideoJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void updateVideoMetaData(List<VideoMetaData> list) {
        String sql = """
                    UPDATE video 
                    SET 
                        prev_view_count = ?,
                        view_count = ?, 
                        like_count = ?, 
                        comment_count = ?
                    WHERE video_id = ?
                """;

        jdbcTemplate.batchUpdate(sql, list, 500,
                (ps, dto) -> {
                    ps.setLong(1, dto.prevViewCount());
                    ps.setLong(2, dto.viewCount());
                    ps.setLong(3, dto.likeCount());
                    ps.setLong(4, dto.commentCount());
                    ps.setString(5, dto.videoId());
                });
    }

    /**
     * 전체 비디오의 trend_score를 DB 레벨에서 단일 쿼리로 재계산.
     * 애플리케이션 메모리를 사용하지 않음.
     */
    public int updateAllTrendScores() {
        String sql = """
                UPDATE video SET trend_score =
                    (0.40 * LOG10(1 + GREATEST(CAST((view_count - prev_view_count) AS DOUBLE) / (prev_view_count + 10), 0)))
                  + (0.20 * SQRT(GREATEST(view_count - prev_view_count, 0)))
                  + (0.25 * (1.0 / SQRT(GREATEST(TIMESTAMPDIFF(HOUR, published_at, NOW()), 1))))
                """;

        return jdbcTemplate.update(sql);
    }
}
