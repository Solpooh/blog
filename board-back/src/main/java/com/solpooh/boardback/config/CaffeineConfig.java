package com.solpooh.boardback.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CaffeineConfig {
    @Bean
    public Cache<String, Boolean> videoIdCache() {
        return Caffeine.newBuilder()
                .maximumSize(200_000)
//                .expireAfterWrite(Duration.ofDays(1)) // TTL
                .build();
    }
}
