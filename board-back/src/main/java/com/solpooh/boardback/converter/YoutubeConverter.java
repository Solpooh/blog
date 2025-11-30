package com.solpooh.boardback.converter;

import com.google.api.services.youtube.model.*;
import com.solpooh.boardback.dto.common.VideoListResponse;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.GetChannelResponse;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public class YoutubeConverter {
    private YoutubeConverter(){}
    public static VideoEntity toVideoEntity(Activity activity, ChannelEntity channel) {
        return VideoEntity.builder()
                .videoId(
                        activity.getContentDetails().getUpload().getVideoId()
                )
                .title(cleanInvalidUTF16(activity.getSnippet().getTitle()))
                .description(cleanInvalidUTF16(activity.getSnippet().getDescription()))
                .thumbnail(
                        activity.getSnippet().getThumbnails().getHigh().getUrl()
                )
                .publishedAt(
                        LocalDateTime.parse(
                                activity.getSnippet().getPublishedAt().toStringRfc3339(),
                                DateTimeFormatter.ISO_DATE_TIME))
                .channel(channel)
                .prevViewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .viewCount(0L) // 최초 0으로 저장
                .trendScore(0.000)
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
                videoEntity.getTrendScore()
        );
    }

    public static GetChannelResponse toResponse(ChannelEntity entity) {
        return new GetChannelResponse(
                entity.getChannelId(),
                entity.getTitle(),
                entity.getThumbnail()
        );
    }

    public static VideoMetaData convertToAllMetaData(Video video) {
        VideoContentDetails cd = video.getContentDetails();
        String duration = (cd != null && cd.getDuration() != null)
                ? cd.getDuration()
                : "PT0S";

        int durationSec = parseDuration(duration);
        boolean isShort = isShortVideo(durationSec);

        return new VideoMetaData(
                video.getId(),
                convertToLong(video.getStatistics().getViewCount()), // prevViewCount도 똑같이 설정
                convertToLong(video.getStatistics().getViewCount()),
                convertToLong(video.getStatistics().getLikeCount()),
                convertToLong(video.getStatistics().getCommentCount()),
                isShort,
                video.getSnippet().getTags(),
                0.000
        );
    }

    public static void updateVideoEntity(VideoEntity entity, VideoMetaData dto) {
        entity.setPrevViewCount(dto.viewCount());
        entity.setViewCount(dto.viewCount());
        entity.setLikeCount(dto.likeCount());
        entity.setCommentCount(dto.commentCount());
        entity.setShort(dto.isShort());
        entity.setTags(dto.tags());
    }

    public static VideoMetaData convertToMetaData(Video video) {
        return VideoMetaData.builder()
                .viewCount(convertToLong(video.getStatistics().getViewCount()))
                .likeCount(convertToLong(video.getStatistics().getLikeCount()))
                .commentCount(convertToLong(video.getStatistics().getCommentCount()))
                .build();
    }

    private static int parseDuration(String isoDuration) {
        return (int) Duration.parse(isoDuration).getSeconds();
    }

    private static boolean isShortVideo(int durationSeconds) {
        return durationSeconds < 60;
    }
    private static Long convertToLong(BigInteger val) {
        return val == null ? 0L : val.longValue();
    }
    private static String cleanInvalidUTF16(String input) {
        if (input == null) return "";

        StringBuilder sb = new StringBuilder();
        input.codePoints().forEach(cp -> {
            if (Character.isValidCodePoint(cp)) {
                sb.append(Character.toChars(cp));
            }
        });
        return sb.toString();
    }
}
