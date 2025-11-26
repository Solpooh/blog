package com.solpooh.boardback.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInitializer {
    private final CacheService cacheService;

    @PostConstruct
    public void init() {
        cacheService.loadAllFromDB();
        System.out.println("캐시 load 실행!!");
    }
}
