package com.solpooh.boardback.service.implement;


import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoResponse;
import com.solpooh.boardback.dto.object.AdminVideoItem;
import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.elasticsearch.VideoDocument;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.VideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImplement implements VideoService {
    private final VideoRepository videoRepository;
    private final CacheService cacheService;
    private final ChannelRepository channelRepository;
    private final ElasticsearchOperations elasticsearchOperations;


    @Override
    public GetVideoListResponse getLatestVideoList(Pageable pageable) {
        Page<VideoEntity> videoEntities =
                videoRepository.getLatestVideo(pageable, "ko");

        List<VideoResponse> videoList = videoEntities.getContent()
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoResponse> pagedList = Pagination.of(videoEntities, videoList);

        return new GetVideoListResponse(pagedList);
    }

    @Override
    @Transactional(readOnly = true)
    public GetSearchVideoListResponse getSearchVideoList(String searchWord, Pageable pageable) {
        String queryJson = """
                {
                  "function_score": {
                    "query": {
                      "multi_match": {
                        "query": "%s",
                        "fields": ["title^3", "tags^2", "description"]
                      }
                    },
                    "functions": [
                      {
                        "gauss": {
                          "publishedAt": {
                            "origin": "now",
                            "scale": "90d",
                            "decay": 0.5
                          }
                        }
                      },
                      {
                        "field_value_factor": {
                          "field": "viewCount",
                          "modifier": "log1p",
                          "factor": 0.0001
                        }
                      }
                    ],
                    "score_mode": "sum",
                    "boost_mode": "sum"
                  }
                }
                """.formatted(searchWord);

        Query query = new StringQuery(queryJson, pageable);

        SearchHits<VideoDocument> hits =
                elasticsearchOperations.search(query, VideoDocument.class);

        // List - 검색 결과 순서 유지
        List<String> videoIds = hits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent().getVideoId())
                .toList();

        List<VideoEntity> videoEntities = videoRepository.findByVideoIdIn(videoIds);

        // JPA 조회 결과는 순서 유지 X - Map을 사용한 재정렬
        Map<String, VideoEntity> map = videoEntities.stream()
                .collect(Collectors.toMap(VideoEntity::getVideoId, v -> v));

        List<VideoEntity> sorted = videoIds.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();

        List<VideoResponse> videoList = sorted
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoResponse> pagedList = Pagination.ofFromSearch(videoList, pageable, hits.getTotalHits());

        return new GetSearchVideoListResponse(pagedList);
    }

    @Override
    public GetHotVideoListResponse getHotVideoList() {
        List<VideoEntity> videoEntities = videoRepository.getHotVideoList();
        List<VideoResponse> videoList = videoEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetHotVideoListResponse(videoList);
    }

    @Override
    public GetTopViewVideoListResponse getTopViewVideoList() {
        List<VideoEntity> videoEntities = videoRepository.getTopViewVideoList();
        List<VideoResponse> videoList = videoEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetTopViewVideoListResponse(videoList);
    }

    @Override
    public GetShortsVideoListResponse getShortsVideoList() {
        List<VideoEntity> videoEntities = videoRepository.getShortsVideoList();
        List<VideoResponse> videoList = videoEntities.stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        return new GetShortsVideoListResponse(videoList);
    }

    @Override
    public GetAdminVideoListResponse getAdminVideoList(Pageable pageable) {
        Page<VideoEntity> videoEntities = videoRepository.getAllVideos(pageable);

        List<AdminVideoItem> videoList = videoEntities.getContent()
                .stream()
                .map(AdminVideoItem::new)
                .toList();

        return new GetAdminVideoListResponse(
                videoList,
                videoEntities.getNumber(),
                videoEntities.getTotalPages(),
                videoEntities.getTotalElements()
        );
    }

    @Override
    public GetAdminVideoListResponse searchAdminVideosByChannel(String channelTitle, Pageable pageable) {
        Page<VideoEntity> videoEntities = videoRepository.searchVideosByChannelTitle(channelTitle, pageable);

        List<AdminVideoItem> videoList = videoEntities.getContent()
                .stream()
                .map(AdminVideoItem::new)
                .toList();

        return new GetAdminVideoListResponse(
                videoList,
                videoEntities.getNumber(),
                videoEntities.getTotalPages(),
                videoEntities.getTotalElements()
        );
    }
}
