package com.solpooh.boardback.batch;

import com.solpooh.boardback.entity.BatchHistoryEntity;
import com.solpooh.boardback.entity.ImageEntity;
import com.solpooh.boardback.repository.ImageRepository;
import com.solpooh.boardback.service.BatchHistoryService;
import com.solpooh.boardback.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageScheduler {

    private final FileService fileService;
    private final ImageRepository imageRepository;
    private final BatchHistoryService batchHistoryService;

    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void deleteImage() {
        String jobName = "IMAGE_CLEANUP";
        log.info("[BATCH:{}] 시작", jobName);
        BatchHistoryEntity history = batchHistoryService.start(jobName);

        try {
            List<ImageEntity> deleteImages = imageRepository.getDeleteImage();

            if (deleteImages.isEmpty()) {
                batchHistoryService.success(history, 0);
                log.info("[BATCH:{}] 삭제할 이미지 없음", jobName);
                return;
            }

            // 1. DB 먼저 삭제 (트랜잭션 롤백 가능)
            imageRepository.deleteAll(deleteImages);

            // 2. S3 삭제 (개별 실패 허용)
            int successCount = 0;
            int failCount = 0;

            for (ImageEntity image : deleteImages) {
                try {
                    fileService.deleteFromS3(image.getImage());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.warn("[BATCH:{}] S3 삭제 실패 (수동 정리 필요): {}", jobName, image.getImage());
                }
            }

            batchHistoryService.success(history, deleteImages.size());
            log.info("[BATCH:{}] 완료 - DB삭제: {}건, S3성공: {}건, S3실패: {}건, 소요: {}ms",
                    jobName, deleteImages.size(), successCount, failCount, history.getDurationMs());

        } catch (Exception e) {
            batchHistoryService.fail(history, e);
            log.error("[BATCH:{}] 실패: {}", jobName, e.getMessage(), e);
        }
    }
}
