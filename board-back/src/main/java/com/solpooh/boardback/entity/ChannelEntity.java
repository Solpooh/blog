package com.solpooh.boardback.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.solpooh.boardback.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "channel")
public class ChannelEntity extends BaseTimeEntity {
    @Id
    @Column(name = "channel_id", unique = true, nullable = false)
    private String channelId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String thumbnail;
    @Column(name = "custom_url", nullable = false)
    private String customUrl;
    @Column(length = 2)
    private String lang;
    @Column(nullable = false)
    private String category;

//    @JsonManagedReference
//    @JsonIgnore => 직렬화/역직렬화 모두 제외
    @JsonBackReference
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoEntity> videos;
}
