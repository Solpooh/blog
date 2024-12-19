package com.solpooh.boardback.config.S3Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
@Profile("dev")
public class DevS3Config {
    @Value("${spring.cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Bean
    public S3AsyncClient devS3AsyncClient() {
        AwsBasicCredentials cred = AwsBasicCredentials.create(accessKey, secretKey);
        return S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(cred))
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}