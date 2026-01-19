package com.solpooh.boardback.service.youtube;

import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoMetaData;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponse;
import com.solpooh.boardback.elasticsearch.VideoIndexService;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.event.NewVideosCollectedEvent;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.fetcher.YoutubeApiFetcher;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Phase 1: 영상 수집 전용 서비스
 * - 외부 API 호출 (트랜잭션 외부)
 * - DB 저장 + ES 인덱싱 (단일 트랜잭션)
 * - Transcript 처리 이벤트 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCollectorService {
    private static final int CHUNK_SIZE = 50;

    private final CacheService cacheService;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;
    private final YoutubeApiFetcher youtubeApiFetcher;
    private final ExecutorService videoFetchExecutor;
    private final VideoIndexService videoIndexService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 영상 수집 메인 메서드
     * 1. Activities API 병렬 호출 (트랜잭션 외부)
     * 2. Video API 호출 (트랜잭션 외부)
     * 3. DB 저장 + ES 인덱싱 (트랜잭션 내부)
     * 4. Transcript 처리 이벤트 발행
     */
    public PostVideoResponse collectVideos() {
        List<String> channelIds = channelRepository.findAllIds();
        if (channelIds.isEmpty()) {
            throw new CustomException(ResponseApi.NOT_EXISTED_CHANNEL);
        }

        // Phase 1-1: Activities API 병렬 호출 (트랜잭션 외부)
        List<Activity> activities = fetchActivitiesParallel(channelIds);

        // Phase 1-2: 신규 영상 필터링
        List<String> existingVideoIds = cacheService.getAllIds();
        List<ActivityWithChannel> newActivities = filterNewActivities(activities, existingVideoIds);

        if (newActivities.isEmpty()) {
            log.info("신규 영상 없음");
            return new PostVideoResponse(Collections.emptyList());
        }

        // Phase 1-3: Video API로 메타데이터 조회 (트랜잭션 외부)
        List<String> newVideoIds = newActivities.stream()
                .map(a -> a.activity.getContentDetails().getUpload().getVideoId())
                .toList();
        Map<String, VideoMetaData> metaDataMap = fetchVideoMetadata(newVideoIds);

        // Phase 1-4: DB 저장 + ES 인덱싱 (단일 트랜잭션)
        List<VideoEntity> savedVideos = saveAndIndexVideos(newActivities, metaDataMap);
        List<String> savedVideoIds = savedVideos.stream()
                .map(VideoEntity::getVideoId)
                .toList();

        log.info("신규 영상 {}개 저장 완료", savedVideoIds.size());

        // Phase 2 트리거: Transcript 비동기 처리 이벤트 발행
        if (!savedVideoIds.isEmpty()) {
            eventPublisher.publishEvent(new NewVideosCollectedEvent(this, savedVideoIds));
        }

        return new PostVideoResponse(savedVideoIds);
    }

    /**
     * Activities API 병렬 호출
     */
    private List<Activity> fetchActivitiesParallel(List<String> channelIds) {
        List<Future<List<Activity>>> futures = new ArrayList<>();

        for (String channelId : channelIds) {
            futures.add(videoFetchExecutor.submit(() -> {
                try {
                    return youtubeApiFetcher.fetchActivityList(channelId);
                } catch (Exception e) {
                    log.warn("Activities API 호출 실패 - channelId: {}, error: {}", channelId, e.getMessage());
                    return Collections.emptyList();
                }
            }));
        }

        List<Activity> activities = new ArrayList<>();
        for (Future<List<Activity>> future : futures) {
            try {
                activities.addAll(future.get());
            } catch (Exception e) {
                log.warn("Activities 결과 수집 실패: {}", e.getMessage());
            }
        }

        return activities;
    }

    /**
     * 신규 영상만 필터링
     */
    private List<ActivityWithChannel> filterNewActivities(List<Activity> activities, List<String> existingVideoIds) {
        Set<String> existingSet = new HashSet<>(existingVideoIds);
        List<ActivityWithChannel> newActivities = new ArrayList<>();

        for (Activity activity : activities) {
            if (activity.getContentDetails().getUpload() == null) continue;

            String videoId = activity.getContentDetails().getUpload().getVideoId();
            if (existingSet.contains(videoId)) continue;

            String channelId = activity.getSnippet().getChannelId();
            newActivities.add(new ActivityWithChannel(activity, channelId));
            existingSet.add(videoId); // 중복 방지
        }

        return newActivities;
    }

    /**
     * Video API로 메타데이터 조회 (50개씩 chunk)
     */
    private Map<String, VideoMetaData> fetchVideoMetadata(List<String> videoIds) {
        Map<String, VideoMetaData> result = new HashMap<>();

        List<List<String>> chunks = chunk(videoIds, CHUNK_SIZE);
        for (List<String> chunkIds : chunks) {
            try {
                List<Video> videos = youtubeApiFetcher.fetchAllVideoData(chunkIds);
                for (Video video : videos) {
                    if (video != null) {
                        result.put(video.getId(), YoutubeConverter.convertToAllMetaData(video));
                    }
                }
            } catch (Exception e) {
                log.warn("Video API 호출 실패 - chunk size: {}, error: {}", chunkIds.size(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * DB 저장 + ES 인덱싱 (단일 트랜잭션)
     */
    @Transactional
    public List<VideoEntity> saveAndIndexVideos(List<ActivityWithChannel> activities,
                                                  Map<String, VideoMetaData> metaDataMap) {
        List<VideoEntity> videoEntities = new ArrayList<>();

        for (ActivityWithChannel item : activities) {
            ChannelEntity channelRef = channelRepository.getReferenceById(item.channelId);
            VideoEntity video = YoutubeConverter.toVideoEntity(item.activity, channelRef);

            // 메타데이터가 있으면 업데이트
            String videoId = video.getVideoId();
            VideoMetaData metaData = metaDataMap.get(videoId);
            if (metaData != null) {
                YoutubeConverter.updateVideoEntity(video, metaData);
            }

            videoEntities.add(video);
        }

        List<VideoEntity> savedEntities = videoRepository.saveAll(videoEntities);

        // ES 인덱싱 (신규 영상만)
        videoIndexService.indexVideos(savedEntities);

        return savedEntities;
    }

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }

    private record ActivityWithChannel(Activity activity, String channelId) {}
}
