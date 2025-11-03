package com.solpooh.boardback.converter;

import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.solpooh.boardback.dto.object.VideoListResponse;
import com.solpooh.boardback.dto.response.youtube.GetChannelResponse;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class YoutubeConverter {
    private YoutubeConverter(){}
    public static VideoEntity toVideoEntity(Activity activity, ChannelEntity channel) {
        if (activity.getContentDetails().getUpload() == null) return null;

        return VideoEntity.builder()
                .videoId(
                        activity.getContentDetails().getUpload().getVideoId()
                )
                .title(activity.getSnippet().getTitle())
                .thumbnail(
                        activity.getSnippet().getThumbnails().getHigh().getUrl()
                )
                .publishedAt(
                        LocalDateTime.parse(
                                activity.getSnippet().getPublishedAt().toStringRfc3339(),
                                DateTimeFormatter.ISO_DATE_TIME))
                .channel(channel)
                .build();
    }

    public static VideoListResponse toResponse(VideoEntity videoEntity) {
        return new VideoListResponse(
                videoEntity.getVideoId(),
                videoEntity.getTitle(),
                videoEntity.getThumbnail(),
                videoEntity.getPublishedAt().toString(),
                videoEntity.getChannel().getTitle(),
                videoEntity.getChannel().getChannelId(),
                videoEntity.getChannel().getThumbnail()
        );
    }

    public static GetChannelResponse toResponse(ChannelEntity entity) {
        return new GetChannelResponse(
                entity.getChannelId(),
                entity.getTitle(),
                entity.getThumbnail()
        );
    }

    public static ChannelEntity toChannelEntity(Channel channel) {
        ChannelSnippet snippet = channel.getSnippet();

        return ChannelEntity.builder()
                .channelId(channel.getId())
                .title(snippet.getTitle())
                .thumbnail(snippet.getThumbnails().getDefault().getUrl())
                .customUrl(
                        Optional.ofNullable(snippet.getCustomUrl())
                                .orElse("unknown")
                )
                .lang("ko")
                .category("dev")
                .build();
    }
}
