package com.solpooh.boardback.config.S3Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
@Profile("prod")
public class ProdS3Config {
    @Bean
    public S3AsyncClient prodS3AsyncClient() {
        return S3AsyncClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.create()) // IAM Role 자동 적용
                .build();
    }
}