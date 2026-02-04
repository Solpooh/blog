package com.solpooh.boardback.service.implement;


import com.solpooh.boardback.cache.CacheService;
import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.converter.YoutubeConverter;
import com.solpooh.boardback.dto.common.VideoResponse;
import com.solpooh.boardback.dto.object.AdminVideoItem;
import com.solpooh.boardback.dto.response.youtube.*;
import com.solpooh.boardback.elasticsearch.VideoDocument;
import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.enums.MainCategory;
import com.solpooh.boardback.enums.SortType;
import com.solpooh.boardback.enums.SubCategory;
import com.solpooh.boardback.repository.ChannelRepository;
import com.solpooh.boardback.repository.VideoRepository;
import com.solpooh.boardback.service.ChannelService;
import com.solpooh.boardback.service.VideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
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
    private final ChannelService channelService;


    @Override
    public GetVideoListResponse getLatestVideoList(Pageable pageable) {
        return getVideoList(pageable, SortType.LATEST);
    }

    @Override
    public GetVideoListResponse getVideoList(Pageable pageable, SortType sortType) {
        Page<VideoEntity> videoEntities =
                videoRepository.getVideoListWithSort(pageable, "ko", sortType);

        List<VideoResponse> videoList = videoEntities.getContent()
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoResponse> pagedList = Pagination.of(videoEntities, videoList);
        Long totalChannelCount = channelService.getTotalChannelCount();

        return new GetVideoListResponse(pagedList, totalChannelCount);
    }

    @Override
    @Transactional(readOnly = true)
    public GetSearchVideoListResponse getSearchVideoList(String searchWord, Pageable pageable) {
        return getSearchVideoList(searchWord, pageable, SortType.RELEVANCE);
    }

    @Override
    @Transactional(readOnly = true)
    public GetSearchVideoListResponse getSearchVideoList(String searchWord, Pageable pageable, SortType sortType) {
        String queryJson = buildSearchQuery(searchWord, sortType, null, null);
        StringQuery query = new StringQuery(queryJson, pageable);

        // RELEVANCE가 아닌 경우 별도 정렬 설정
        applySort(query, sortType);

        SearchHits<VideoDocument> hits =
                elasticsearchOperations.search(query, VideoDocument.class);

        List<VideoResponse> videoList = convertSearchHitsToVideoList(hits);
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
    public GetCategoryStatsResponse getCategoryStats() {
        // GROUP BY 한방 쿼리로 모든 카테고리 통계 조회
        var statsResults = videoRepository.getCategoryStatsGroupBy();

        // MainCategory별로 그룹핑
        Map<String, Map<String, Long>> categoryMap = new HashMap<>();
        Map<String, Long> mainCategoryTotals = new HashMap<>();

        for (var result : statsResults) {
            String mainCat = result.getMainCategory();
            String subCat = result.getSubCategory();
            Long count = result.getCount();

            // MainCategory별 SubCategory 맵 생성
            categoryMap.computeIfAbsent(mainCat, k -> new HashMap<>())
                    .put(subCat != null ? subCat : "NONE", count);

            // MainCategory별 총합 계산
            mainCategoryTotals.merge(mainCat, count, Long::sum);
        }

        // Response 객체 생성
        List<GetCategoryStatsResponse.MainCategoryStats> categoryStatsList = new ArrayList<>();

        for (MainCategory mainCategory : MainCategory.values()) {
            String mainCatName = mainCategory.name();
            Long mainCount = mainCategoryTotals.get(mainCatName);

            // count가 0이거나 없는 대분류는 제외
            if (mainCount == null || mainCount == 0) {
                continue;
            }

            // 해당 대분류의 소분류 통계
            Map<String, Long> subMap = categoryMap.get(mainCatName);
            List<GetCategoryStatsResponse.SubCategoryStats> subCategoryStatsList = new ArrayList<>();

            if (subMap != null) {
                for (SubCategory subCategory : SubCategory.values()) {
                    if (subCategory.getMainCategory() == mainCategory) {
                        String subCatName = subCategory.name();
                        Long subCount = subMap.get(subCatName);

                        if (subCount != null && subCount > 0) {
                            subCategoryStatsList.add(new GetCategoryStatsResponse.SubCategoryStats(
                                    subCatName,
                                    subCategory.getDisplayName(),
                                    subCount
                            ));
                        }
                    }
                }
            }

            // count 내림차순 정렬
            subCategoryStatsList.sort((a, b) -> Long.compare(b.count(), a.count()));

            categoryStatsList.add(new GetCategoryStatsResponse.MainCategoryStats(
                    mainCatName,
                    mainCategory.getDisplayName(),
                    mainCount,
                    subCategoryStatsList
            ));
        }

        // count 내림차순 정렬
        categoryStatsList.sort((a, b) -> Long.compare(b.count(), a.count()));

        return new GetCategoryStatsResponse(categoryStatsList);
    }

    @Override
    public GetVideoListResponse getVideosByMainCategory(MainCategory mainCategory, Pageable pageable, SortType sortType) {
        Page<VideoEntity> videoEntities =
                videoRepository.getVideosByMainCategory(mainCategory, pageable, sortType);

        List<VideoResponse> videoList = videoEntities.getContent()
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoResponse> pagedList = Pagination.of(videoEntities, videoList);
        Long totalChannelCount = channelService.getTotalChannelCount();

        return new GetVideoListResponse(pagedList, totalChannelCount);
    }

    @Override
    public GetVideoListResponse getVideosBySubCategory(MainCategory mainCategory, SubCategory subCategory, Pageable pageable, SortType sortType) {
        Page<VideoEntity> videoEntities =
                videoRepository.getVideosBySubCategory(mainCategory, subCategory, pageable, sortType);

        List<VideoResponse> videoList = videoEntities.getContent()
                .stream()
                .map(YoutubeConverter::toResponse)
                .toList();

        Pagination<VideoResponse> pagedList = Pagination.of(videoEntities, videoList);
        Long totalChannelCount = channelService.getTotalChannelCount();

        return new GetVideoListResponse(pagedList, totalChannelCount);
    }

    @Override
    @Transactional(readOnly = true)
    public GetSearchVideoListResponse searchInMainCategory(MainCategory mainCategory, String searchWord, Pageable pageable, SortType sortType) {
        String queryJson = buildSearchQuery(searchWord, sortType, mainCategory, null);
        StringQuery query = new StringQuery(queryJson, pageable);

        // RELEVANCE가 아닌 경우 별도 정렬 설정
        applySort(query, sortType);

        SearchHits<VideoDocument> hits =
                elasticsearchOperations.search(query, VideoDocument.class);

        List<VideoResponse> videoList = convertSearchHitsToVideoList(hits);
        Pagination<VideoResponse> pagedList = Pagination.ofFromSearch(videoList, pageable, hits.getTotalHits());

        return new GetSearchVideoListResponse(pagedList);
    }

    @Override
    @Transactional(readOnly = true)
    public GetSearchVideoListResponse searchInSubCategory(MainCategory mainCategory, SubCategory subCategory, String searchWord, Pageable pageable, SortType sortType) {
        String queryJson = buildSearchQuery(searchWord, sortType, mainCategory, subCategory);
        StringQuery query = new StringQuery(queryJson, pageable);

        // RELEVANCE가 아닌 경우 별도 정렬 설정
        applySort(query, sortType);

        SearchHits<VideoDocument> hits =
                elasticsearchOperations.search(query, VideoDocument.class);

        List<VideoResponse> videoList = convertSearchHitsToVideoList(hits);
        Pagination<VideoResponse> pagedList = Pagination.ofFromSearch(videoList, pageable, hits.getTotalHits());

        return new GetSearchVideoListResponse(pagedList);
    }

    // ==================== Helper Methods ====================

    /**
     * ES 검색 쿼리 생성 (카테고리 필터 지원)
     * 정렬은 applySort 메서드에서 별도로 처리
     */
    private String buildSearchQuery(String searchWord, SortType sortType, MainCategory mainCategory, SubCategory subCategory) {
        // 필터 조건 생성
        StringBuilder filterBuilder = new StringBuilder();
        if (mainCategory != null) {
            filterBuilder.append("{ \"term\": { \"mainCategory\": \"")
                    .append(mainCategory.name())
                    .append("\" } }");
            if (subCategory != null) {
                filterBuilder.append(", { \"term\": { \"subCategory\": \"")
                        .append(subCategory.name())
                        .append("\" } }");
            }
        }

        String filterClause = filterBuilder.length() > 0
                ? ", \"filter\": [" + filterBuilder + "]"
                : "";

        // function_score 사용 여부 (RELEVANCE일 때만)
        if (sortType == SortType.RELEVANCE) {
            return """
                {
                  "function_score": {
                    "query": {
                      "bool": {
                        "must": {
                          "multi_match": {
                            "query": "%s",
                            "fields": ["title^3", "tags^2", "description", "transcript"]
                          }
                        }%s
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
                """.formatted(searchWord, filterClause);
        } else {
            // LATEST, VIEWS: 단순 bool 쿼리 (정렬은 applySort에서 처리)
            return """
                {
                  "bool": {
                    "must": {
                      "multi_match": {
                        "query": "%s",
                        "fields": ["title^3", "tags^2", "description", "transcript"]
                      }
                    }%s
                  }
                }
                """.formatted(searchWord, filterClause);
        }
    }

    /**
     * StringQuery에 정렬 조건 적용
     */
    private void applySort(StringQuery query, SortType sortType) {
        if (sortType == SortType.LATEST) {
            query.addSort(Sort.by(Sort.Direction.DESC, "publishedAt"));
        } else if (sortType == SortType.VIEWS) {
            query.addSort(Sort.by(Sort.Direction.DESC, "viewCount"));
        }
        // RELEVANCE는 ES 스코어 기반 정렬이므로 별도 처리 불필요
    }

    /**
     * ES SearchHits → VideoResponse 리스트 변환
     */
    private List<VideoResponse> convertSearchHitsToVideoList(SearchHits<VideoDocument> hits) {
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

        return sorted.stream()
                .map(YoutubeConverter::toResponse)
                .toList();
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
