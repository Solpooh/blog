package com.solpooh.boardback.elasticsearch;

import com.solpooh.boardback.entity.VideoEntity;
import com.solpooh.boardback.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return doc;
    }
}
