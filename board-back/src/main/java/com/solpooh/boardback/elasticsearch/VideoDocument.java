package com.solpooh.boardback.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "video")
public class VideoDocument {
    @Id
    private String videoId;
    // full-text 검색: analyzed text (기본 tokenizer 적용)
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    // 정렬/범위쿼리용
    @Field(type = FieldType.Date)
    private LocalDateTime publishedAt;
    @Field(type = FieldType.Long)
    private Long viewCount;
    @Field(type = FieldType.Boolean)
    private Boolean isShort;
}
