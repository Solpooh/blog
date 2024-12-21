package com.solpooh.boardback.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadToS3(MultipartFile file);
//    Resource getImage(String fileName);
    void deleteToS3(String fileName);
}
