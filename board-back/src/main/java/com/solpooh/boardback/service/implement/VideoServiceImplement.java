package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoListResponse;
import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.VideoService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
    private final ChannelRepository channelRepository;
//    private static final int CHUNK_SIZE = 50;

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

    // POST: 모든 채널의 비디오 정보 저장하기
    @Override
    @Transactional
    public PostVideoResponse postVideo() {
        // channel 전부 조회
        List<ChannelEntity> channelList = channelRepository.findAll();
        Set<String> videoIds = videoRepository.findAllIds();

        if (channelList.isEmpty()) throw new CustomException(ResponseApi.NOT_EXISTED_CHANNEL);

        channelList.stream()
                .flatMap(channel -> fetchVideoFromYoutube(channel.getChannelId()).stream()
                    .map(activity -> YoutubeConverter.toVideoEntity(activity, channel))
                        .filter(Objects::nonNull)
                )
                .filter(video -> !videoIds.contains(video.getVideoId()))
                .forEach(videoRepository::save);

        return new PostVideoResponse();
    }


    private List<Activity> fetchVideoFromYoutube(String channelId) {
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

    @Override
    public DeleteVideoResponse deleteVideo(String videoId) {

        VideoEntity videoEntity = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        videoRepository.delete(videoEntity);
        log.info("VideoEntity 삭제 성공");

        return new DeleteVideoResponse();
    }

    @Transactional
    public void postVideoInfo() {
        // 1. 모든 videoId 불러오기
        List<String> videoIdList = new ArrayList<>(videoRepository.findAllIds());
        int chunkSize = 50;

        // 2. 50개 단위로 chunk 나누기
        List<List<String>> chunks = chunk(videoIdList, chunkSize);

        for (List<String> chunk : chunks) {
            try {
                // 3. Youtube API 요청
                YouTube.Videos.List request = youtube.videos()
                        .list("statistics, contentDetails")
                        .setId(String.join(",", chunk))
                        .setKey(apiKey);

                List<Video> response = request.execute().getItems();

                // 4. videoId -> 통계값 매핑
                Map<String, VideoMetaDTO> videoMap = response.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                Video::getId,
                                v -> {
                                    int durationSec = parseDurationToSeconds(
                                            v.getContentDetails().getDuration()
                                    );
                                    boolean isShort = isShortVideo(durationSec);

                                    return new VideoMetaDTO(
                                            convertToLong(v.getStatistics().getViewCount()),
                                            convertToLong(v.getStatistics().getLikeCount()),
                                            convertToLong(v.getStatistics().getCommentCount()),
                                            isShort
                                    );
                                }
                        ));

                // 5. DB 조회
                List<VideoEntity> entities = videoRepository.findByVideoIdIn(chunk);

                // 6. statistics 반영
                for (VideoEntity entity : entities) {
                    VideoMetaDTO dto = videoMap.get(entity.getVideoId());
                    if (dto != null) {
                        updateVideoEntity(entity, dto);
                    }
                }
                // 7. Batch 저장
                videoRepository.saveAll(entities);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateVideoEntity(VideoEntity entity, VideoMetaDTO dto) {
        // 6-1 이전 조회수 백업
        long prev = entity.getViewCount() != null ? entity.getViewCount() : 0;
        entity.setPrevViewCount(prev);

        // 6-2 신규 조회수 반영
        entity.setViewCount(dto.getViewCount());
        entity.setLikeCount(dto.getLikeCount());
        entity.setCommentCount(dto.getCommentCount());
        entity.setShort(dto.isShort());

        // 6-3 상승 비율 계산
        long diff = dto.getViewCount() - prev;
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

    private int parseDurationToSeconds(String isoDuration) {
        return (int) Duration.parse(isoDuration).getSeconds();
    }

    private boolean isShortVideo(int durationSeconds) {
        return durationSeconds < 60;
    }
    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }

    private Long convertToLong(BigInteger val) {
        return val == null ? 0L : val.longValue();
    }
    @Getter
    @AllArgsConstructor
    static class VideoMetaDTO {
        private Long viewCount;
        private Long likeCount;
        private Long commentCount;
        private boolean isShort;
    }
}
