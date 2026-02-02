package com.solpooh.boardback.elasticsearch;

import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoIndexService {
    private final VideoRepository videoRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Value("${index.page-size:100}")
    private int pageSize;

    @Transactional(readOnly = true)
    public void indexAll(){
        // 인덱스가 없으면 매핑을 생성
        IndexCoordinates index = IndexCoordinates.of("video");
        IndexOperations indexOps = elasticsearchOperations.indexOps(index);

        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(VideoDocument.class));
        }

        int page = 0;
        Page<VideoEntity> pageResult;

        do {
            PageRequest pageRequest = PageRequest.of(page, pageSize);
            pageResult = videoRepository.findAll(pageRequest);

            List<VideoDocument> docs = pageResult.getContent().stream()
                    .map(this::toDocument)
                    .toList();

            if (!docs.isEmpty()) {
                // VideoDocument -> IndexQuery 변환
                List<IndexQuery> queries = docs.stream()
                        .map(doc -> new IndexQueryBuilder()
                                .withId(doc.getVideoId())
                                .withObject(doc)
                                .build()
                        )
                        .toList();

                // Bulk Index => 문서가 없으면 생성, 있으면 덮어씀
                elasticsearchOperations.bulkIndex(queries, index);
            }

            page++;
        } while (pageResult.hasNext());
    }

    private VideoDocument toDocument(VideoEntity entity) {
        VideoDocument doc = new VideoDocument();
        doc.setVideoId(entity.getVideoId());
        doc.setTitle(entity.getTitle());
        doc.setDescription(entity.getDescription());
        doc.setTags(entity.getTags());
        doc.setPublishedAt(entity.getPublishedAt().toString());
        doc.setViewCount(entity.getViewCount());
        doc.setIsShort(entity.isShort());
        doc.setMainCategory(entity.getMainCategory() != null ? entity.getMainCategory().name() : null);
        doc.setSubCategory(entity.getSubCategory() != null ? entity.getSubCategory().name() : null);

        return doc;
    }

    /**
     * 신규 영상만 ES에 인덱싱
     */
    public void indexVideos(List<VideoEntity> videos) {
        if (videos == null || videos.isEmpty()) {
            return;
        }

        IndexCoordinates index = IndexCoordinates.of("video");
        ensureIndexExists(index);

        List<IndexQuery> queries = videos.stream()
                .map(this::toDocument)
                .map(doc -> new IndexQueryBuilder()
                        .withId(doc.getVideoId())
                        .withObject(doc)
                        .build())
                .toList();

        elasticsearchOperations.bulkIndex(queries, index);
        log.info("ES 인덱싱 완료: {}개 영상", videos.size());
    }

    /**
     * transcript 필드만 부분 업데이트
     */
    public void updateTranscriptField(String videoId, String transcript) {
        IndexCoordinates index = IndexCoordinates.of("video");

        Document updateDoc = Document.create();
        updateDoc.put("transcript", transcript);

        UpdateQuery updateQuery = UpdateQuery.builder(videoId)
                .withDocument(updateDoc)
                .build();

        try {
            elasticsearchOperations.update(updateQuery, index);
            log.debug("ES transcript 업데이트 완료: {}", videoId);
        } catch (Exception e) {
            log.warn("ES transcript 업데이트 실패: {} - {}", videoId, e.getMessage());
        }
    }

    /**
     * category 필드만 부분 업데이트
     */
    public void updateCategoryFields(String videoId, String mainCategory, String subCategory) {
        IndexCoordinates index = IndexCoordinates.of("video");

        Document updateDoc = Document.create();
        updateDoc.put("mainCategory", mainCategory);
        updateDoc.put("subCategory", subCategory);

        UpdateQuery updateQuery = UpdateQuery.builder(videoId)
                .withDocument(updateDoc)
                .build();

        try {
            elasticsearchOperations.update(updateQuery, index);
            log.debug("ES category 업데이트 완료: {} - main={}, sub={}", videoId, mainCategory, subCategory);
        } catch (Exception e) {
            log.warn("ES category 업데이트 실패: {} - {}", videoId, e.getMessage());
        }
    }

    private void ensureIndexExists(IndexCoordinates index) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(index);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(VideoDocument.class));
        }
    }

    /**
     * 단일 비디오 삭제
     */
    public void deleteVideo(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return;
        }

        IndexCoordinates index = IndexCoordinates.of("video");
        try {
            elasticsearchOperations.delete(videoId, index);
            log.info("ES 인덱스 삭제 완료: {}", videoId);
        } catch (Exception e) {
            log.warn("ES 인덱스 삭제 실패: {} - {}", videoId, e.getMessage());
        }
    }

    /**
     * 여러 비디오 일괄 삭제
     */
    public void deleteVideos(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return;
        }

        IndexCoordinates index = IndexCoordinates.of("video");
        try {
            // Bulk delete
            videoIds.forEach(videoId -> {
                try {
                    elasticsearchOperations.delete(videoId, index);
                } catch (Exception e) {
                    log.warn("ES 인덱스 삭제 실패: {} - {}", videoId, e.getMessage());
                }
            });
            log.info("ES 인덱스 일괄 삭제 완료: {}개", videoIds.size());
        } catch (Exception e) {
            log.error("ES 인덱스 일괄 삭제 중 오류: {}", e.getMessage(), e);
        }
    }
}
