package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.solpooh.boardback.converter.VideoConverter;
import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.dto.response.youtube.DeleteVideoResponseDto;
import com.solpooh.boardback.dto.response.youtube.GetChannelResponseDto;
import com.solpooh.boardback.dto.response.youtube.GetVideoListResponseDto;
import com.solpooh.boardback.dto.response.youtube.PostVideoResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class VideoServiceImplement implements VideoService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;

    @Override
    public ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList(Pageable pageable) {
        Page<VideoEntity> videoEntities;

        try {

            videoEntities = videoRepository.getLatestVideo(pageable, "dev", "ko");
            if (videoEntities.isEmpty()) return GetVideoListResponseDto.videoNotFound();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return GetVideoListResponseDto.success(videoEntities);
    }

    // POST: 모든 채널의 비디오 정보 저장하기
    @Override
    public ResponseEntity<? super PostVideoResponseDto> postVideo() {
        try {

            List<ChannelEntity> channelList = channelRepository.findAll();
            if (channelList.isEmpty()) return GetChannelResponseDto.channelNotFound();

            channelList.forEach(this::saveVideoEntity);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return PostVideoResponseDto.success();
    }

    @Override
    public ResponseEntity<? super DeleteVideoResponseDto> deleteVideo(String videoId) {
        try {

            VideoEntity videoEntity = videoRepository.findByVideoId(videoId);
            if (videoEntity == null) return DeleteVideoResponseDto.noExistVideo();

            videoRepository.delete(videoEntity);
            log.info("VideoEntity 삭제 성공");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return DeleteVideoResponseDto.success();
    }

    private List<Activity> fetchVideoList(String channelId) {
        try {

            YouTube.Activities.List request = youtube.activities()
                    .list("snippet, contentDetails")
                    .setChannelId(channelId)
                    .setMaxResults(10L) // 유튜버가 매일 10개 영상을 올릴 가능성이 없음.
                    .setKey(apiKey);

            return request.execute().getItems();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void saveVideoEntity(ChannelEntity channel) {
        String channelId = channel.getChannelId();

        try {
            // 채널 정보를 통해 API 요청 후 응답받기
            List<Activity> activities = fetchVideoList(channelId);

            activities.stream()
                    .map(activity -> VideoConverter.toVideoEntity(activity, channel))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(video -> !videoRepository.existsById(video.getVideoId()))
                    .forEach(video -> {
                        videoRepository.save(video);
                        log.info("✅ 저장된 영상: {}", video.getVideoId());
                    });

        } catch (Exception e) {
            log.error("비디오 저장 에러 발생", e);
        }
    }
}
