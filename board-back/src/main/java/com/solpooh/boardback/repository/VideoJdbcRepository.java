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
                view_count = ?, 
                like_count = ?, 
                comment_count = ?,
                is_short = ?
            WHERE video_id = ?
        """;

        jdbcTemplate.batchUpdate(sql, list, 500,
                (ps, dto) -> {
                    ps.setLong(1, dto.viewCount());
                    ps.setLong(2, dto.likeCount());
                    ps.setLong(3, dto.commentCount());
                    ps.setBoolean(4, dto.isShort());
                    ps.setString(5, dto.videoId());
                });
    }
}
