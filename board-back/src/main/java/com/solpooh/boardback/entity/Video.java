package com.solpooh.boardback.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.solpooh.boardback.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "video")
public class Video extends BaseTimeEntity {
    @Id
    @Column(name = "video_id", unique = true, nullable = false)
    private String videoId;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String thumbnail;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
}
