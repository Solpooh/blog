package com.solpooh.boardback.repository.ifs;

import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.enums.MainCategory;
import com.solpooh.boardback.enums.SortType;
import com.solpooh.boardback.enums.SubCategory;
import com.solpooh.boardback.repository.resultSet.CategoryStatsResultSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VideoRepositoryIf {
    Page<VideoEntity> getLatestVideo(Pageable pageable, String lang);
    List<VideoEntity> getHotVideoList();
    List<VideoEntity> getTopViewVideoList();
    List<VideoEntity> getShortsVideoList();

    // Admin
    Page<VideoEntity> getAllVideos(Pageable pageable);
    Page<VideoEntity> searchVideosByChannelTitle(String channelTitle, Pageable pageable);

    // 정렬 옵션 적용
    Page<VideoEntity> getVideoListWithSort(Pageable pageable, String lang, SortType sortType);

    // 카테고리 조회
    Page<VideoEntity> getVideosByMainCategory(MainCategory mainCategory, Pageable pageable, SortType sortType);
    Page<VideoEntity> getVideosBySubCategory(MainCategory mainCategory, SubCategory subCategory, Pageable pageable, SortType sortType);

    // 카테고리 통계 (Deprecated)
//    Long countByMainCategory(MainCategory mainCategory);
//    Long countBySubCategory(MainCategory mainCategory, SubCategory subCategory);

    // 카테고리 통계 (GROUP BY 한방 쿼리)
    List<CategoryStatsResultSet> getCategoryStatsGroupBy();
}
