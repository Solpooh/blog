package com.solpooh.boardback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
    @Bean
    public ExecutorService videoFetchExecutor() {
        return Executors.newFixedThreadPool(32);
    }
}
