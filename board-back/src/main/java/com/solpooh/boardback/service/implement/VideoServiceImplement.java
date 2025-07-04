package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.google.api.services.youtube.model.Video;
import com.solpooh.boardback.dto.response.board.GetVideoListResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.provider.ChannelList;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class VideoServiceImplement implements VideoService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;

    // GET: 모든 채널의 비디오 정보 가져오기
//    @Override
//    public ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList() {
//        List<VideoEntity> videoEntities = videoRepository.getLatestVideo("dev", "ko");
//
//        return GetVideoListResponseDto.success(videoEntities);
//    }

    @Override
    public ResponseEntity<? super GetVideoListResponseDto> getLatestVideoList(Pageable pageable) {
        Page<VideoEntity> videoEntities = videoRepository.getLatestVideo(pageable, "dev", "ko");
        return GetVideoListResponseDto.success(videoEntities);
    }

    // POST: 해당 채널의 비디오 정보 저장하기
    @Override
    public String postVideo() {
        // 1. 하드 코딩한 channelList에서 channelEntity 추출
        for (String channelId : ChannelList.CHANNEL_IDS) {
            ChannelEntity channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다: " + channelId));

            try {
                // 2. 해당 id로 API 요청 channelId => videoList
                YouTube.Activities.List request = youtube.activities()
                        .list("snippet, contentDetails")
                        .setChannelId(channelId)
                        .setMaxResults(10L)
                        .setKey(apiKey);

                ActivityListResponse response = request.execute();
                List<Activity> items = response.getItems();

                for (Activity activity: items) {
                    if (activity.getContentDetails().getUpload() != null && !videoRepository.existsById(activity.getContentDetails().getUpload().getVideoId())) {
                        String videoId = activity.getContentDetails().getUpload().getVideoId();
                        String title = activity.getSnippet().getTitle();
                        String thumbnail = activity.getSnippet().getThumbnails().getHigh().getUrl();
                        String publishedAt = activity.getSnippet().getPublishedAt().toStringRfc3339();

                        // 3. 반환 응답 필드를 DB에 저장
                        VideoEntity video = VideoEntity.builder()
                                .videoId(videoId)
                                .title(title)
                                .thumbnail(thumbnail)
                                .publishedAt(LocalDateTime.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME))
                                .channel(channel)
                                .build();

                        videoRepository.save(video);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "비디오 저장 완료";
    }
}
