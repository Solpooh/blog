package com.solpooh.boardback.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "board_list_view")
@Table(name = "board_list_view")
public class BoardListViewEntity {
    @Id
    private Long boardNumber;
    private String title;
    private String content;
    private String category;
    private String titleImage;
    private int viewCount;
    private int favoriteCount;
    private int commentCount;
    private LocalDateTime writeDatetime;
    private String writerEmail;
    private String writerNickname;
    private String writerProfileImage;
}
