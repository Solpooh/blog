package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ifs.VideoRepositoryIf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VideoRepository extends JpaRepository<VideoEntity, String>, VideoRepositoryIf {
    Optional<VideoEntity> findByVideoId(String videoId);
    @Query("select v.videoId from VideoEntity v")
    Set<String> findAllIds();

    // 50개 chunk에 해당하는 엔티티 조회
    List<VideoEntity> findByVideoIdIn(List<String> videoIds);
}
