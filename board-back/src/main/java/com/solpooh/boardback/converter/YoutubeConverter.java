package com.solpooh.boardback.converter;

import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.solpooh.boardback.dto.common.VideoListResponse;
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
                .description(activity.getSnippet().getDescription())
                .thumbnail(
                        activity.getSnippet().getThumbnails().getHigh().getUrl()
                )
                .publishedAt(
                        LocalDateTime.parse(
                                activity.getSnippet().getPublishedAt().toStringRfc3339(),
                                DateTimeFormatter.ISO_DATE_TIME))
                .channel(channel)
                .viewCount(0L) // 최초 0으로 저장
                .build();
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

    public static VideoListResponse toResponse(VideoEntity videoEntity) {
        return new VideoListResponse(
                videoEntity.getVideoId(),
                videoEntity.getTitle(),
                videoEntity.getDescription(),
                videoEntity.getThumbnail(),
                videoEntity.getPublishedAt().toString(),
                videoEntity.getChannel().getTitle(),
                videoEntity.getChannel().getCustomUrl(),
                videoEntity.getChannel().getThumbnail(),
                videoEntity.getPrevViewCount(),
                videoEntity.getViewCount(),
                videoEntity.getLikeCount(),
                videoEntity.getCommentCount(),
                videoEntity.getTrendScore(),
                videoEntity.isShort()
        );
    }

    public static GetChannelResponse toResponse(ChannelEntity entity) {
        return new GetChannelResponse(
                entity.getChannelId(),
                entity.getTitle(),
                entity.getThumbnail()
        );
    }

}
