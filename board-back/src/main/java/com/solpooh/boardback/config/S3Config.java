package com.solpooh.boardback.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class S3Config {
    @Value("${spring.cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${spring.cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Bean
    public S3AsyncClient S3AsyncClient() {
        AwsBasicCredentials cred = AwsBasicCredentials.create(accessKey, secretKey);
        final Region region = Region.AP_NORTHEAST_2;

        return S3AsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(cred))
                .region(region)
                .serviceConfiguration(S3Configuration.builder().build())
                .build();
    }

}
