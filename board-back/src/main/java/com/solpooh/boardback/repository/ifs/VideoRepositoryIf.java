package com.solpooh.boardback.repository.ifs;

import com.solpooh.boardback.entity.VideoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VideoRepositoryIf {
    Page<VideoEntity> getLatestVideo(Pageable pageable, String category, String lang);
//    Page<VideoEntity> getSearchVideoList(String searchWord, String type, Pageable pageable);
    List<VideoEntity> getHotVideoList();
    List<VideoEntity> getTopViewVideoList();
    List<VideoEntity> getShortsVideoList();
}
