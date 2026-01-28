package com.solpooh.boardback.dto.object;

import com.solpooh.boardback.entity.VideoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminVideoItem {
    private String videoId;
    private String videoTitle;
    private String videoThumbnail;
    private String channelId;
    private String channelTitle;
    private String channelThumbnail;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminVideoItem(VideoEntity video) {
        this.videoId = video.getVideoId();
        this.videoTitle = video.getTitle();
        this.videoThumbnail = video.getThumbnail();
        this.channelId = video.getChannel().getChannelId();
        this.channelTitle = video.getChannel().getTitle();
        this.channelThumbnail = video.getChannel().getThumbnail();
        this.publishedAt = video.getPublishedAt();
        this.createdAt = video.getCreatedAt();
        this.updatedAt = video.getUpdatedAt();
    }
}
