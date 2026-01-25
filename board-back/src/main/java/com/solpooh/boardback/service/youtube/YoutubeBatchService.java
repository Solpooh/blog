package com.solpooh.boardback.service.youtube;

import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponse;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.fetcher.YoutubeApiFetcher;
import com.solpooh.boardback.repository.VideoJdbcRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeBatchService {
    private static final int CHUNK_SIZE = 50;
    private final VideoRepository videoRepository;
    private final YoutubeApiFetcher youtubeApiService;
    private final VideoJdbcRepository videoJdbcRepository;
    private final VideoCollectorService videoCollectorService;
    private final CacheService cacheService;

    /**
     * 영상 수집 메인 메서드
     * VideoCollectorService로 위임하여 최적화된 파이프라인 실행
     */
    public PostVideoResponse postVideo() {
        return videoCollectorService.collectVideos();
    }

    /**
     * Score 기반의 조회수, 댓글수, 좋아요 수 갱신
     * @return 업데이트된 영상 수
     */
    @Transactional
    public int updateVideoData() {
        List<VideoEntity> videoEntities = videoRepository.findAll();

        // trend score 정렬
        Comparator<VideoEntity> descByScore =
                Comparator.comparingDouble(VideoEntity::getTrendScore).reversed();

        // 상위 400개 videoId
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
        int totalUpdated = 0;

        for (List<String> chunk : chunks) {
            // Youtube API 요청 + 신규 MetaData 생성
            List<Video> apiList = youtubeApiService.fetchVideoData(chunk);

            Map<String, VideoMetaData> metaMap = apiList.stream()
                    .collect(Collectors.toMap(
                            Video::getId,
                            YoutubeConverter::convertToMetaData
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
                totalUpdated += mergedList.size();
            }
        }

        return totalUpdated;
    }

    public DeleteVideoResponse deleteVideo(String videoId) {

        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_VIDEO));

        videoRepository.delete(videoEntity);
        cacheService.remove(videoId);
        return new DeleteVideoResponse();
    }

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }

    /**
     * 전체 영상의 트렌드 스코어 재계산
     * @return 업데이트된 영상 수
     */
    @Transactional
    public int updateVideoScore() {
        List<VideoEntity> videoEntities = videoRepository.findAll();
        List<VideoMetaData> updateList = videoEntities.stream()
                .map(entity -> VideoMetaData.builder()
                        .videoId(entity.getVideoId())
                        .trendScore(calculateScore(entity))
                        .build())
                .toList();

        videoJdbcRepository.updateTrendScore(updateList);
        return updateList.size();
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
