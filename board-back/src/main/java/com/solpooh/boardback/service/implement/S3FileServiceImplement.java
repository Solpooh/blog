package com.solpooh.boardback.service.implement;
import com.solpooh.boardback.repository.ImageRepository;
import com.solpooh.boardback.service.FileService;
import lombok.RequiredArgsConstructor;
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
    @Value("${file.url}")
    private String fileUrl;
    @Value("${spring.cloud.aws.s3.bucketName}")
    private String bucketName;

    private final S3AsyncClient s3AsyncClient;

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

            s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(file.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
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
