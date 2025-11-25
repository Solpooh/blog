package com.solpooh.boardback.converter;

import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.dto.common.VideoListResponse;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.GetChannelResponse;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public static VideoMetaData toResponse(Video video) {
        int durationSec = parseDuration(video.getContentDetails().getDuration());
        boolean isShort = isShortVideo(durationSec);

        return new VideoMetaData(
                convertToLong(video.getStatistics().getViewCount()),
                convertToLong(video.getStatistics().getLikeCount()),
                convertToLong(video.getStatistics().getCommentCount()),
                isShort,
                video.getSnippet().getTags()
        );
    }

    public static void updateVideoEntity(VideoEntity entity, VideoMetaData dto) {
        // 6-1 이전 조회수 백업
        long prev = entity.getViewCount() != null ? entity.getViewCount() : 0;

        entity.setPrevViewCount(prev);

        // 6-2 신규 조회수 반영
        entity.setViewCount(dto.viewCount());
        entity.setLikeCount(dto.likeCount());
        entity.setCommentCount(dto.commentCount());
        entity.setShort(dto.isShort());

        // 6-3 상승 비율 계산
        long diff = dto.viewCount() - prev;
        double ratio = (double) diff / (prev + 1); // prev = 0 대비

        // 6-4
        // ratio + log → 상승 비율 기반, 대형 채널 편향 제거
        // timeComponent → 제곱근 감쇠, 최신 영상에게 가산점
        double logComponent = Math.log10(1 + Math.max(ratio, 0));

        long hours = Duration.between(
                entity.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Instant.now()
        ).toHours();

        double timeDecay = 1 / Math.sqrt(hours + 2);
        double trendScore = logComponent + timeDecay;

        entity.setTrendScore(trendScore);

        // 6-5 Tag List 반영
        entity.setTags(dto.tagList());
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
