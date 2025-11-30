package com.solpooh.boardback.service.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class YoutubeApiService {
    @Value("${youtube.api.key}")
    private String apiKey;
    private final YouTube youtube;

    public List<Activity> fetchActivityList(String channelId) {
        try {

            YouTube.Activities.List request = youtube.activities()
                    .list("snippet, contentDetails")
                    .setChannelId(channelId)
                    .setMaxResults(5L)
                    .setKey(apiKey);

            return request.execute().getItems();

        } catch (IOException e) {
            log.error("YouTube API 영상 목록 조회 실패: {}", channelId, e);
            return Collections.emptyList();
        }
    }

    public List<Video> fetchAllVideoData(List<String> videoIds) {
        try {

            YouTube.Videos.List request = youtube.videos()
                    .list("snippet, statistics, contentDetails")
                    .setId(String.join(",", videoIds))
                    .setKey(apiKey);

            return request.execute().getItems();

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Video> fetchVideoData(List<String> chunk) {
        try {

            YouTube.Videos.List request = youtube.videos()
                    .list("statistics")
                    .setId(String.join(",", chunk))
                    .setKey(apiKey);

            return request.execute().getItems();

        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
