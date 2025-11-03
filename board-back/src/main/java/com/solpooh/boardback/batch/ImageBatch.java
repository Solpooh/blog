package com.solpooh.boardback.batch;

import com.solpooh.boardback.entity.ImageEntity;
import com.solpooh.boardback.repository.ImageRepository;
import com.solpooh.boardback.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ImageBatch {
    private final FileService fileService;
    private final ImageRepository imageRepository;

//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 12 * * ?")
    public void deleteImage() {
        log.info("Batch 작업 실행 !!");
        List<ImageEntity> deleteImages = imageRepository.getDeleteImage();

        deleteImages.forEach(image ->
                fileService.deleteFromS3(image.getImage()));

        imageRepository.deleteAll(deleteImages);
    }
}
