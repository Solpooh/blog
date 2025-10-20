package com.solpooh.boardback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "search_log")
@Table(name = "search_log")
public class SearchLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequence;
    private String searchWord;
    private String relationWord;
    private boolean relation;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SearchLogEntity(String searchWord, String relationWord, boolean relation) {
        this.searchWord = searchWord;
        this.relationWord = relationWord;
        this.relation = relation;
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
