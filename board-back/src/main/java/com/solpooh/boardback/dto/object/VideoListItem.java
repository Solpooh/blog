package com.solpooh.boardback.dto.object;

import com.solpooh.boardback.entity.VideoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoListItem {
    private String videoId;
    private String title;
    private String thumbnail;
    private String publishedAt;
    private String channelTitle;
    private String channelId;
    private String channelThumbnail;

    public VideoListItem(VideoEntity videoEntity) {
        this.videoId = videoEntity.getVideoId();
        this.title = videoEntity.getTitle();
        this.thumbnail = videoEntity.getThumbnail();
        this.publishedAt = videoEntity.getPublishedAt().toString();
        this.channelTitle = videoEntity.getChannel().getTitle();
        this.channelId = videoEntity.getChannel().getChannelId();
        this.channelThumbnail = videoEntity.getChannel().getThumbnail();
    }

    public static List<VideoListItem> getList(List<VideoEntity> videoEntities) {
        List<VideoListItem> videoList = new ArrayList<>();
        for (VideoEntity videoEntity : videoEntities) {
            videoList.add(new VideoListItem(videoEntity));
        }
        return videoList;
    }
}
