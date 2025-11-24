package com.solpooh.boardback.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.solpooh.boardback.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "prev_view_count")
    private Long prevViewCount;
    @Column(name = "view_count")
    private Long viewCount;
    @Column(name = "like_count")
    private Long likeCount;
    @Column(name = "comment_count")
    private Long commentCount;
    @Column(name = "trend_score")
    private Double trendScore; // 조회수 상승 비율 기반 점수
    @Column(name = "is_short")
    private boolean isShort;
}
