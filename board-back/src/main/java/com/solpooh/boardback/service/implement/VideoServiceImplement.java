package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImplement implements VideoService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;

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
//                .filter(video -> !videoRepository.existsById(video.getVideoId()))
                .forEach(videoRepository::save);

        return new PostVideoResponse();
    }


    private List<Activity> fetchVideoFromYoutube(String channelId) {
        try {

            YouTube.Activities.List request = youtube.activities()
                    .list("snippet, contentDetails")
                    .setChannelId(channelId)
                    .setMaxResults(10L) // 10개의 영상
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
}
