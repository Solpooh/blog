package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.solpooh.boardback.dto.response.video.GetVideoListResponseDto;
import com.solpooh.boardback.dto.response.video.PostVideoResponseDto;
import com.solpooh.boardback.entity.ChannelEntity;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.provider.ChannelList;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        Page<VideoEntity> videoEntities = videoRepository.getLatestVideo(pageable, "dev", "ko");
        return GetVideoListResponseDto.success(videoEntities);
    }

    // POST: 해당 채널의 비디오 정보 저장하기
    @Override
    public ResponseEntity<? super PostVideoResponseDto> postVideo() {
        // 1. 하드 코딩한 channelList에서 channelEntity 추출
        for (String channelId : ChannelList.CHANNEL_IDS) {
            try {
                ChannelEntity channel = channelRepository.findById(channelId)
                        .orElseThrow(() -> new IllegalArgumentException("채널이 존재하지 않습니다: " + channelId));
                saveVideoList(channel);
            } catch (Exception e) {
                log.info("❌ 채널 처리 실패 ({}): {}", channelId, e.getMessage());
//                webhookNotifier.notifyError("채널 처리 실패: " + channelId, e);
            }
        }

        return PostVideoResponseDto.success();
    }

    private void saveVideoList(ChannelEntity channel) throws IOException {
        YouTube.Activities.List request = youtube.activities()
                .list("snippet, contentDetails")
                .setChannelId(channel.getChannelId())
                .setMaxResults(10L) // 유튜버가 매일 10개 영상을 올릴 가능성이 없음.
                .setKey(apiKey);

        ActivityListResponse response = request.execute();
        List<Activity> items = response.getItems();


        for (Activity activity : items) {
            if (activity.getContentDetails().getUpload() != null) {
                String videoId = activity.getContentDetails().getUpload().getVideoId();

                if (!videoRepository.existsById(videoId)) {
                    try {
                        VideoEntity video = VideoEntity.builder()
                                .videoId(videoId)
                                .title(activity.getSnippet().getTitle())
                                .thumbnail(activity.getSnippet().getThumbnails().getHigh().getUrl())
                                .publishedAt(LocalDateTime.parse(activity.getSnippet().getPublishedAt().toStringRfc3339(), DateTimeFormatter.ISO_DATE_TIME))
                                .channel(channel)
                                .build();

                        videoRepository.save(video);
                        log.info("✅ 새 비디오 저장됨: {}", videoId);

                    } catch (Exception e) {
                        log.info("❌ 비디오 저장 실패 ({}): {}", videoId, e.getMessage());
//                        webhookNotifier.notifyError("비디오 저장 실패: " + videoId, e);
                    }
                }
            }
        }
    }
}
