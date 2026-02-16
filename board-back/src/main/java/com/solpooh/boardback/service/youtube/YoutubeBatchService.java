package com.solpooh.boardback.service.youtube;

import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponse;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.fetcher.YoutubeApiFetcher;
import com.solpooh.boardback.repository.VideoJdbcRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

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
    private final VideoIndexService videoIndexService;

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
        // trendScore 상위 400개만 DB에서 조회 (findAll() 대신)
        List<VideoEntity> topVideos = videoRepository.findTopByTrendScore(PageRequest.of(0, 400));

        List<String> topVideoIds = topVideos.stream()
                .map(VideoEntity::getVideoId)
                .toList();

        Map<String, VideoEntity> entityMap = topVideos.stream()
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

    @Transactional
    public DeleteVideoResponse deleteVideo(String videoId) {

        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_VIDEO));

        // DB 삭제
        videoRepository.delete(videoEntity);

        // Cache 삭제
        cacheService.remove(videoId);

        // Elasticsearch 삭제
        videoIndexService.deleteVideo(videoId);

        return new DeleteVideoResponse();
    }

    @Transactional
    public int deleteVideos(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return 0;
        }

        List<VideoEntity> videoEntities = videoRepository.findByVideoIdIn(videoIds);

        if (videoEntities.isEmpty()) {
            throw new CustomException(ResponseApi.NOT_EXISTED_VIDEO);
        }

        // DB 삭제
        videoRepository.deleteAll(videoEntities);

        // Cache 삭제
        cacheService.removeAll(videoIds);

        // Elasticsearch 삭제
        videoIndexService.deleteVideos(videoIds);

        return videoEntities.size();
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
     * DB 레벨에서 단일 쿼리로 처리하여 애플리케이션 메모리 사용 없음
     * @return 업데이트된 영상 수
     */
    @Transactional
    public int updateVideoScore() {
        return videoJdbcRepository.updateAllTrendScores();
    }

}
