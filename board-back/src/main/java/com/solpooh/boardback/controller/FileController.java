package com.solpooh.boardback.controller;

import com.solpooh.boardback.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    // 이미지 업로드
    @PostMapping("/upload")
    public String upload(
            @RequestParam("file") MultipartFile file
    ) {
        return fileService.uploadToS3(file);
    }

    // 이미지 URL 반환
//    @GetMapping(value = "{fileName}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
//    public Resource getImage(
//            @PathVariable("fileName") String fileName
//    ) {
//        Resource resource = fileService.getImage(fileName);
//        return resource;
//    }
}
