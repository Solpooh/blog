package com.solpooh.boardback.converter;

import com.google.api.services.youtube.model.Activity;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class VideoConverter {
    public static Optional<VideoEntity> toVideoEntity(Activity activity, ChannelEntity channel) {
        if (activity.getContentDetails().getUpload() == null) return Optional.empty();

        String videoId = activity.getContentDetails().getUpload().getVideoId();

        return Optional.of(
                VideoEntity.builder()
                        .videoId(videoId)
                        .title(activity.getSnippet().getTitle())
                        .thumbnail(activity.getSnippet().getThumbnails().getHigh().getUrl())
                        .publishedAt(LocalDateTime.parse(activity.getSnippet().getPublishedAt().toStringRfc3339(),
                                DateTimeFormatter.ISO_DATE_TIME))
                        .channel(channel)
                        .build()
        );
    }
}
