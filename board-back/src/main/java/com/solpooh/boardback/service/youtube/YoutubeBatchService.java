package com.solpooh.boardback.service.youtube;

import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponse;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.fetcher.YoutubeApiFetcher;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoJdbcRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeBatchService {
    private static final int CHUNK_SIZE = 50;
    private final CacheService cacheService;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;
    private final YoutubeApiFetcher youtubeApiService;
    private final VideoJdbcRepository videoJdbcRepository;
    private final ExecutorService videoFetchExecutor;
    private final VideoIndexService videoIndexService;
    private final TranscriptService transcriptService;
    @Transactional
    public PostVideoResponse postVideo() {
        // 기존의 channelId 조회
        List<String> channelIds = channelRepository.findAllIds();
        if (channelIds.isEmpty()) throw new CustomException(ResponseApi.NOT_EXISTED_CHANNEL);

        // Cache에서 기존의 videoId 조회
        List<String> videoIds = cacheService.getAllIds();

        // 병렬 처리 시 사용
        List<Future<List<Activity>>> futures = new ArrayList<>();

        for (String channelId : channelIds) {
            futures.add(videoFetchExecutor.submit(() -> {
                // I/O 작업
                return youtubeApiService.fetchActivityList(channelId);
            }));
        }

        // 결과 병합
        List<Activity> activities = new ArrayList<>();
        for (Future<List<Activity>> future : futures) {
            try {
                activities.addAll(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 신규 영상 처리
        List<VideoEntity> newVideos = new ArrayList<>();
        List<String> newVideoIds = new ArrayList<>();

        for (Activity activity : activities) {
            if (activity.getContentDetails().getUpload() == null) continue;
            String videoId = activity.getContentDetails().getUpload().getVideoId();

            if (videoIds.contains(videoId)) continue;

            // channelId 기반 Proxy Entity 생성
            String channelId = activity.getSnippet().getChannelId();
            ChannelEntity channelRef = channelRepository.getReferenceById(channelId);

            VideoEntity video = YoutubeConverter.toVideoEntity(activity, channelRef);
            newVideos.add(video);
            newVideoIds.add(videoId);
        }

        videoRepository.saveAll(newVideos);

        // 메타데이터 업데이트는 별도 트랜잭션으로 처리 - 추후 chunk로 리팩토링할 것
        updateVideo(newVideoIds);
//        transcriptService(newVideoIds);
        videoIndexService.indexAll();


        return new PostVideoResponse(newVideoIds);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateVideo(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) return;

        List<Video> response = youtubeApiService.fetchAllVideoData(videoIds);
        Map<String, VideoMetaData> videoMap = response.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Video::getId,
                        YoutubeConverter::convertToAllMetaData
                ));

        List<VideoEntity> videoEntities = videoRepository.findAllById(videoIds);
        videoEntities.forEach(entity -> {
            Optional.ofNullable(videoMap.get(entity.getVideoId()))
                    .ifPresent(dto -> YoutubeConverter.updateVideoEntity(entity, dto));
        });

        videoRepository.saveAll(videoEntities);
    }

    @Transactional
    // Score 기반의 조회수, 댓글수, 좋아요 수 갱신
    public void updateVideoData() {
        List<VideoEntity> videoEntities = videoRepository.findAll();

        // trend score 정렬
        Comparator<VideoEntity> descByScore =
                Comparator.comparingDouble(VideoEntity::getTrendScore).reversed();

        // 상위 200개 videoId
        List<String> topVideoIds = videoEntities.stream()
                .sorted(descByScore)
                .limit(400)
                .map(VideoEntity::getVideoId)
                .toList();

        // Entity Map 구성 (기존 DB 값 활용)
        Map<String, VideoEntity> entityMap = videoEntities.stream()
                .collect(Collectors.toMap(VideoEntity::getVideoId, v -> v));
        // videoId 50개 단위로 chunk 나누기
        var chunks = chunk(topVideoIds, CHUNK_SIZE);

        for (List<String> chunk : chunks) {
            // Youtube API 요청 + 신규 MetaData 생성
            List<Video> apiList = youtubeApiService.fetchVideoData(chunk);

            Map<String, VideoMetaData> metaMap = apiList.stream()
                    .collect(Collectors.toMap(
                            Video::getId,
                            YoutubeConverter::convertToMetaData // 신규 값만 생성함
                    ));

            // entity + meta를 합쳐 업데이트용 MetaData 생성(이전 조회수 백업을 위해)
            List<VideoMetaData> mergedList = chunk.stream()
                    .map(id -> {
                        VideoEntity entity = entityMap.get(id);
                        VideoMetaData newMeta = metaMap.get(id);

                        if (entity == null || newMeta == null) return null;

                        return VideoMetaData.builder()
                                .videoId(id)
                                .prevViewCount(entity.getViewCount())
                                .viewCount(newMeta.viewCount())
                                .likeCount(newMeta.likeCount())
                                .commentCount(newMeta.commentCount())
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (!mergedList.isEmpty()) {
                videoJdbcRepository.updateVideoMetaData(mergedList);
            }

        }
    }

    public DeleteVideoResponse deleteVideo(String videoId) {

        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        videoRepository.delete(videoEntity);
//        cacheService.remove();
        return new DeleteVideoResponse();
    }

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }

    public void updateVideoScore() {
        List<VideoEntity> videoEntities = videoRepository.findAll();
        List<VideoMetaData> updateList = videoEntities.stream()
                .map(entity -> VideoMetaData.builder()
                        .videoId(entity.getVideoId())
                        .trendScore(calculateScore(entity))
                        .build())
                .toList();

        videoJdbcRepository.updateTrendScore(updateList);
    }

    public static double calculateScore(VideoEntity entity) {
        long prev = entity.getPrevViewCount();
        long curr = entity.getViewCount();

        long diff = Math.max(curr - prev, 0);

        // A. 증가율(rateScore)
        double rate = (double) diff / (prev + 10);
        double rateScore = Math.log10(1 + Math.max(rate, 0));

        // B. 절대 증가량(deltaScore)
        double deltaScore = Math.sqrt(diff);

        // C. 최신성(latestScore) - 점수 더 많이 주기
        long hours = Duration.between(
                entity.getPublishedAt().atZone(ZoneId.systemDefault()).toInstant(),
                Instant.now()
        ).toHours();
        hours = Math.max(1, hours);
        double latestScore = 1.0 / Math.sqrt(hours);

        return (0.40 * rateScore)
                + (0.20 * deltaScore)
                + (0.25 * latestScore);
    }
}
