package com.solpooh.boardback.dto.object;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solpooh.boardback.entity.BoardListViewEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardListItem {
    private int boardNumber;
    private String title;
    private String content;
    private String category;
    private String boardTitleImage;
    private int favoriteCount;
    private int commentCount;
    private int viewCount;
    private String writeDatetime;
    private String writerNickname;
    private String writerProfileImage;

    public BoardListItem(BoardListViewEntity boardListViewEntity) {
        this.boardNumber = boardListViewEntity.getBoardNumber();
        this.title = boardListViewEntity.getTitle();
        this.content = parseContent(boardListViewEntity.getContent());
        this.category = boardListViewEntity.getCategory();
        this.boardTitleImage = boardListViewEntity.getTitleImage();
        this.favoriteCount = boardListViewEntity.getFavoriteCount();
        this.commentCount = boardListViewEntity.getCommentCount();
        this.viewCount = boardListViewEntity.getViewCount();
        this.writeDatetime = boardListViewEntity.getWriteDatetime();
        this.writerNickname = boardListViewEntity.getWriterNickname();
        this.writerProfileImage = boardListViewEntity.getWriterProfileImage();
    }

    // JSON 데이터를 문자열로 변환하는 메서드
    private String parseContent(String content) {
        try {
            // JSON 문자열을 JsonNode로 변환
            JsonNode parsedContent = new ObjectMapper().readTree(content);

            // "blocks" 필드에서 배열 데이터 추출
            JsonNode blocks = parsedContent.get("blocks");

            // 각 JsonNode에서 "text" 필드 추출
            return StreamSupport.stream(blocks.spliterator(), false)
                    .map(block -> block.get("text").asText())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));
        } catch (Exception e) {
            // JSON 파싱 실패 시 원본 content 반환
            return content;
        }
    }

    public static List<BoardListItem> getList(List<BoardListViewEntity> boardListViewEntities) {
        return boardListViewEntities.stream()
                .map(BoardListItem::new) // 변환된 생성자 호출
                .collect(Collectors.toList());
    }

}
