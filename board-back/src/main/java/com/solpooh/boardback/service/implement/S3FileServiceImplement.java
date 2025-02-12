package com.solpooh.boardback.service.implement;
import com.solpooh.boardback.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileServiceImplement implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(S3FileServiceImplement.class);

    @Value("${file.url}")
    private String fileUrl;
    @Value("${spring.cloud.aws.s3.bucketName}")
    private String bucketName;

    private final S3AsyncClient s3AsyncClient;
    @PostConstruct
    public void init() {
        if (s3AsyncClient != null) {
            logger.info("S3AsyncClient가 성공적으로 주입되었습니다.");
        } else {
            logger.error("S3AsyncClient가 주입되지 않았습니다.");
        }
    }
    @Override
    public String uploadToS3(MultipartFile file) {
        if (file.isEmpty()) return null;

        // 파일 이름 생성
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
        String uuid = UUID.randomUUID().toString();
        String saveFileName = uuid + extension;

        // S3에 업로드
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(saveFileName)
                    .contentType(file.getContentType())
                    .build();
            logger.info("파일 업로드 시작: " + originalFileName);  // 업로드 시작 로깅

            s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(file.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("파일 업로드 중 예외 발생", e);
            return null;
        }
        return fileUrl + saveFileName;
    }

    @Override
    public void deleteToS3(String fileName) {
        fileName = fileName.substring(fileUrl.length());

        // S3에서 삭제
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3AsyncClient.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
