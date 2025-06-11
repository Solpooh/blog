package com.solpooh.boardback.service.implement;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.solpooh.boardback.entity.Channel;
import com.solpooh.boardback.entity.Video;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class VideoService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;
    @Transactional
    public List<Video> fetchAndSaveLatestVideos(String channelId) throws IOException {
        try {
            YouTube.Search.List request = youtube.search()
                    .list("snippet")
                    .setChannelId(channelId)
                    .setOrder("date")
                    .setMaxResults(3L)
                    .setType("video")
                    .setKey(apiKey);

            SearchListResponse response = request.execute();
            System.out.println(response);
            Channel channel = channelRepository.findById(channelId)
                    .orElseGet(() -> {
                        Channel newChannel = new Channel();
                        newChannel.setChannelId(channelId);
                        newChannel.setTitle("Unknown"); // 실제 채널 정보를 가져오려면 videos.get(0).snippet.channelTitle 사용 가능
                        newChannel.setThumbnail("Unknown");
                        newChannel.setCustomUrl("Unknown");
                        newChannel.setLang("ko");
                        newChannel.setCategory("general");
                        return channelRepository.save(newChannel);
                    });

            // API 응답을 VIDEO 엔티티에 매핑/저장
            List<Video> videos = new ArrayList<>();
            for (SearchResult item : response.getItems()) {
                String videoId = item.getId().getVideoId();
                if (videoId == null) continue;

                Optional<Video> existingVideo = videoRepository.findById(videoId);
                if (existingVideo.isPresent()) continue;

                Video video = new Video();
                video.setVideoId(videoId);
                video.setTitle(item.getSnippet().getTitle());
                video.setThumbnail(item.getSnippet().getThumbnails().getDefault().getUrl());
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(item.getSnippet().getPublishedAt().toStringRfc3339());
                video.setPublishedAt(zonedDateTime.toLocalDateTime());
                video.setChannel(channel);
                videos.add(videoRepository.save(video));
            }
            return videos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch videos from YouTube API", e);
        }
    }
}
