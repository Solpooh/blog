package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ifs.VideoRepositoryIf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VideoRepository extends JpaRepository<VideoEntity, String>, VideoRepositoryIf {
    Optional<VideoEntity> findByVideoId(String videoId);

    boolean existsByVideoId(String videoId);

    @Query("select v.videoId from VideoEntity v")
    List<String> findVideoIds();

    // 50개 chunk에 해당하는 엔티티 조회
    List<VideoEntity> findByVideoIdIn(List<String> videoIds);

//    @Modifying
//    @Query("UPDATE VideoEntity v SET v.transcript = :transcript WHERE v.videoId = :videoId")
//    int updateTranscript(@Param("videoId") String videoId, @Param("transcript") String transcript);
}
