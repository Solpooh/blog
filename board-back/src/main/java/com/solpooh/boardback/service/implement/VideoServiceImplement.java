package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoListResponse;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoJdbcRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.VideoService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImplement implements VideoService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final VideoRepository videoRepository;
    private final VideoJdbcRepository videoJdbcRepository;
    private final CacheService cacheService;
    private final ChannelRepository channelRepository;
    private static final int CHUNK_SIZE = 50;

    @Override
    public GetVideoListResponse getLatestVideoList(Pageable pageable) {
        Page<VideoEntity> videoEntities =
                videoRepository.getLatestVideo(pageable, "dev", "ko");

        List<VideoListResponse> videoList = videoEntities.getContent()
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoListResponse> pagedList = Pagination.of(videoEntities, videoList);

        return new GetVideoListResponse(pagedList);
    }

    @Override
    public GetSearchVideoListResponse getSearchVideoList(String searchWord, String type, Pageable pageable) {
        Page<VideoEntity> videoEntities =
                videoRepository.getSearchVideoList(searchWord, type, pageable);

        List<VideoListResponse> videoList = videoEntities.getContent()
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoListResponse> pagedList = Pagination.of(videoEntities, videoList);

        return new GetSearchVideoListResponse(pagedList);
    }
    @Override
    public GetHotVideoListResponse getHotVideoList() {
        List<VideoEntity> videoEntities = videoRepository.getHotVideoList();
        List<VideoListResponse> videoList = videoEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetHotVideoListResponse(videoList);
    }

    @Override
    public GetTopViewVideoListResponse getTopViewVideoList() {
        List<VideoEntity> videoEntities = videoRepository.getTopViewVideoList();
        List<VideoListResponse> videoList = videoEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetTopViewVideoListResponse(videoList);
    }

    @Override
    public GetShortsVideoListResponse getShortsVideoList() {
        List<VideoEntity> videoEntities = videoRepository.getShortsVideoList();
        List<VideoListResponse> videoList = videoEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetShortsVideoListResponse(videoList);
    }

    // POST: 모든 채널의 비디오 list 저장하기
    @Override
    @Transactional
    public PostVideoResponse postVideo() {
        // channel 전부 조회
        List<ChannelEntity> channelList = channelRepository.findAll();
        List<String> videoIds = videoRepository.findAllIds();

        if (channelList.isEmpty()) throw new CustomException(ResponseApi.NOT_EXISTED_CHANNEL);

        channelList.stream()
                .flatMap(channel -> fetchActivityList(channel.getChannelId()).stream()
                    .map(activity -> YoutubeConverter.toVideoEntity(activity, channel))
                        .filter(Objects::nonNull)
                )
                .filter(video -> !videoIds.contains(video.getVideoId()))
                .forEach(videoRepository::save);

//        cacheService.add
        return new PostVideoResponse();
    }



    @Override
    public DeleteVideoResponse deleteVideo(String videoId) {

        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        videoRepository.delete(videoEntity);
        log.info("VideoEntity 삭제 성공");

//        cacheService.remove();
        return new DeleteVideoResponse();
    }

    private List<Activity> fetchActivityList(String channelId) {
        try {

            YouTube.Activities.List request = youtube.activities()
                    .list("snippet, contentDetails")
                    .setChannelId(channelId)
                    .setMaxResults(20L)
                    .setKey(apiKey);

            return request.execute().getItems();

        } catch (IOException e) {
            log.error("YouTube API 영상 목록 조회 실패: {}", channelId, e);
            return Collections.emptyList();
        }
    }

    private List<Video> fetchVideoList(List<String> chunk) {
        try {

            YouTube.Videos.List request = youtube.videos()
                    .list("statistics")
                    .setId(String.join(",", chunk))
                    .setKey(apiKey);

            return request.execute().getItems();

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    @Transactional
    // "Score 기반의 조회수, 댓글수, 좋아요 수 갱신"
    public void postAllVideoInfo() {
        List<VideoEntity> videoEntities = videoRepository.findAll();
//        videoEntities.forEach(v -> v.setTrendScore(calculateScore(v)));

        Comparator<VideoEntity> descByScore =
                Comparator.comparingDouble(VideoEntity::getTrendScore).reversed();

        List<String> videoIds = videoEntities.stream()
                .sorted(descByScore)
                .limit(200)
                .map(VideoEntity::getVideoId)
                .toList();

        // 2. 50개 단위로 chunk 나누기
        var chunks = chunk(videoIds, CHUNK_SIZE);

        for (List<String> chunk : chunks) {
            List<Video> apiList = fetchVideoList(chunk);

            List<VideoMetaData> updates = apiList.stream()
                    .filter(Objects::nonNull)
                    .map(YoutubeConverter::toResponse)
                    .toList();

            if (!updates.isEmpty()) {
                videoJdbcRepository.updateVideoMetaData(updates);
            }

        }
    }

    @Override
    public void dailyCalculate() {
        List<VideoEntity> videoEntities = videoRepository.findAll();
        videoEntities.forEach(v -> v.setTrendScore(calculateScore(v)));
    }

    public static double calculateScore(VideoEntity entity) {
        long prev = entity.getPrevViewCount() == null ? 0 : entity.getPrevViewCount();
        long curr = entity.getViewCount() == null ? prev : entity.getViewCount();

        long diff = Math.max(curr - prev, 0);

        // A. 증가율(rateScore)
        double rate = (double) diff / (prev + 10);
        double rateScore = Math.log10(1 + Math.max(rate, 0));

        // B. 절대 증가량(deltaScore)
        double deltaScore = Math.sqrt(diff);

        // C. 최신성(latestScore)
        long hours = Duration.between(
                entity.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Instant.now()
        ).toHours();
        hours = Math.max(1, hours);
        double latestScore = 1.0 / Math.sqrt(hours);

        // D. 구독자 기반
        return (0.40 * rateScore)
                + (0.20 * deltaScore)
                + (0.25 * latestScore);
    }

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }
}
