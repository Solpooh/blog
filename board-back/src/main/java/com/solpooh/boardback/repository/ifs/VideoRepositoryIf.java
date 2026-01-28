package com.solpooh.boardback.repository.ifs;

import com.solpooh.boardback.entity.VideoEntity;
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
}
