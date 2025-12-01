package com.solpooh.boardback.entity;

import com.solpooh.boardback.converter.JsonStringListConverter;
import com.solpooh.boardback.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "video")
public class VideoEntity extends BaseTimeEntity {
    @Id
    @Column(name = "video_id", unique = true, nullable = false)
    private String videoId;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String thumbnail;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private ChannelEntity channel;

    @Column(name = "prev_view_count", nullable = false)
    private Long prevViewCount = 0L;
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;
    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;
    @Column(name = "trend_score")
    private Double trendScore; // 조회수 상승 비율 기반 점수
    @Column(name = "is_short")
    private boolean isShort;
    @Column(columnDefinition = "json")
    @Convert(converter = JsonStringListConverter.class)
    private List<String> tags;
}
