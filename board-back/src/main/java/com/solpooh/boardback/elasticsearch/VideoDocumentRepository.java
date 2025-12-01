package com.solpooh.boardback.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoDocumentRepository extends ElasticsearchRepository<VideoDocument, String> {
}
