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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
                videoRepository.getSearchListVideo(searchWord, type, pageable);

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
                    .setMaxResults(10L)
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
    public void postViewCount() {
        // 1. 모든 videoId 불러오기
        List<String> videoIdList = new ArrayList<>(videoRepository.findAllIds());
        int chunkSize = 50;

        // 2. 50개 단위로 chunk 나누기
        List<List<String>> chunks = chunk(videoIdList, chunkSize);

        for (List<String> chunk : chunks) {
            try {
                // 3. Youtube API 요청
                YouTube.Videos.List request = youtube.videos()
                        .list("statistics")
                        .setId(String.join(",", chunk))
                        .setKey(apiKey);

                List<Video> response = request.execute().getItems();

                // 4. videoId -> viewCount 매핑
                Map<String, Long> viewCountMap = response.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                Video::getId,
                                v -> Long.parseLong(String.valueOf(v.getStatistics().getViewCount()))
                        ));

                // 5. DB 조회
                List<VideoEntity> entities = videoRepository.findByVideoIdIn(chunk);

                // 6. statistics 반영
                for (VideoEntity entity : entities) {
                    Long viewCount = viewCountMap.get(entity.getVideoId());
                    if (viewCount != null) {
                        entity.setViewCount(viewCount);
                    }
                }

                // 7. Batch 저장
                videoRepository.saveAll(entities);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }
}
