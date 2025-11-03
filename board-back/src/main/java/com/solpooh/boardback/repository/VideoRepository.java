package com.solpooh.boardback.repository;

import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.ifs.VideoRepositoryIf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<VideoEntity, String>, VideoRepositoryIf {
    Optional<VideoEntity> findByVideoId(String videoId);
}
