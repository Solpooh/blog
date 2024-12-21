package com.solpooh.boardback.batch;

import com.solpooh.boardback.entity.ImageEntity;
import com.solpooh.boardback.repository.ImageRepository;
import com.solpooh.boardback.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageBatch {
    private final FileService fileService;
    private final ImageRepository imageRepository;

    @Scheduled(cron = "0 0 12 * * ?")
    public void cleanUpImages() {
        System.out.println("Batch 작업 실행 !!");
        List<ImageEntity> deleteImages = imageRepository.getDeleteImage();
        List<ImageEntity> imageEntities = new ArrayList<>();

        for (ImageEntity image : deleteImages) {
            String fileName = image.getImage();
            fileService.deleteToS3(fileName);
            imageEntities.add(image);
        }
        imageRepository.deleteAll(imageEntities);
    }

    // search log도 batch 작업 도입

}
