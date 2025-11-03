package com.solpooh.boardback.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadToS3(MultipartFile file);
    void deleteFromS3(String fileName);
}
