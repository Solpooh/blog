package com.solpooh.boardback.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final Cache<String, Boolean> cache;
    private final VideoRepository videoRepository;

    // 서버 시작 시 호출
    @Transactional
    public void loadAllFromDB() {
        List<String> videoIds = videoRepository.findAllIds(); // video_id만 select
        videoIds.forEach(id -> cache.put(id, Boolean.TRUE));
    }

    public void add(String id) {
        cache.put(id, Boolean.TRUE);
    }

    public void remove(String id) {
        cache.invalidate(id);
    }

    public boolean contains(String id) {
        return cache.getIfPresent(id) != null;
    }

    public List<String> getAllIds() {
        return new ArrayList<>(cache.asMap().keySet());
    }

    @Transactional
    public void syncFromDB() {
        List<String> dbIds = videoRepository.findAllIds();

        var dbSet = new HashSet<>(dbIds);

        Set<String> cached = new HashSet<>(cache.asMap().keySet());

        cached.stream()
                .filter(k -> !dbSet.contains(k))
                .forEach(cache::invalidate);
        dbIds.forEach(id -> cache.put(id, Boolean.TRUE));
    }

    // Cache가 비어있는 경우
    @Transactional
    public List<String> getAllIdsOrLoadIfEmpty() {
        var keys = cache.asMap().keySet();
        if (keys.isEmpty()) {
            loadAllFromDB();
            return new ArrayList<>(cache.asMap().keySet());
        }
        return new ArrayList<>(keys);
    }
}
