package com.solpooh.boardback.repository.ifs;

import com.solpooh.boardback.entity.VideoEntity;

import java.util.List;

public interface VideoRepositoryIf {
    List<VideoEntity> findVideosByCategoryAndLang(String category, String lang);
}
