package com.solpooh.boardback.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solpooh.boardback.dto.object.BoardListResponse;
import com.solpooh.boardback.dto.object.CommentResponse;
import com.solpooh.boardback.dto.object.FavoriteResponse;
import com.solpooh.boardback.dto.request.board.PatchBoardRequest;
import com.solpooh.boardback.dto.request.board.PostBoardRequest;
import com.solpooh.boardback.dto.request.board.PostCommentRequest;
import com.solpooh.boardback.dto.response.board.GetBoardDetailResponse;
import com.solpooh.boardback.entity.BoardEntity;
import com.solpooh.boardback.entity.BoardListViewEntity;
import com.solpooh.boardback.entity.CommentEntity;
import com.solpooh.boardback.entity.FavoriteEntity;
import com.solpooh.boardback.repository.resultSet.GetBoardDetailResultSet;
import com.solpooh.boardback.repository.resultSet.GetCommentListResultSet;
import com.solpooh.boardback.repository.resultSet.GetFavoriteListResultSet;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BoardConverter {
    // 유틸성 클래스이므로 인스턴스화 방지
    private BoardConverter() {}

    public static GetBoardDetailResponse toResponse(GetBoardDetailResultSet resultSet, List<String> imageList) {
        return new GetBoardDetailResponse(
                resultSet.getBoardNumber(),
                resultSet.getTitle(),
                resultSet.getContent(),
                resultSet.getCategory(),
                imageList,
                resultSet.getWriteDatetime(),
                resultSet.getWriterEmail(),
                resultSet.getWriterNickname(),
                resultSet.getWriterProfileImage()
        );
    }

    public static FavoriteResponse toResponse(GetFavoriteListResultSet resultSet) {
        return new FavoriteResponse(
                resultSet.getEmail(),
                resultSet.getNickname(),
                resultSet.getProfileImage()
        );
    }

    public static CommentResponse toResponse(GetCommentListResultSet resultSet) {
        return new CommentResponse(
                resultSet.getCommentNumber(),
                resultSet.getNickname(),
                resultSet.getProfileImage(),
                resultSet.getWriteDatetime(),
                resultSet.getContent(),
                resultSet.getUserEmail()
        );
    }

    public static BoardListResponse toResponse(BoardListViewEntity boardListViewEntity) {
        return new BoardListResponse(
                boardListViewEntity.getBoardNumber(),
                boardListViewEntity.getTitle(),
                parseContent(boardListViewEntity.getContent()),
                boardListViewEntity.getCategory(),
                boardListViewEntity.getTitleImage(),
                boardListViewEntity.getFavoriteCount(),
                boardListViewEntity.getCommentCount(),
                boardListViewEntity.getViewCount(),
                boardListViewEntity.getWriteDatetime(),
                boardListViewEntity.getWriterNickname(),
                boardListViewEntity.getWriterProfileImage()
        );
    }

    public static BoardEntity toEntity(PostBoardRequest dto, String email) {
        return BoardEntity.builder()
                .title(dto.title())
                .content(dto.content())
                .category(dto.category())
                .writeDatetime(formatDate())
                .favoriteCount(0)
                .commentCount(0)
                .viewCount(0)
                .writerEmail(email)
                .build();
    }

    public static CommentEntity toEntity(PostCommentRequest dto, Long boardNumber, String email) {
        return CommentEntity.builder()
                .content(dto.content())
                .writeDatetime(formatDate())
                .userEmail(email)
                .boardNumber(boardNumber)
                .build();

    }

    private static String formatDate() {
        Date now = Date.from(Instant.now());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return simpleDateFormat.format(now);
    }
    private static String parseContent(String content) {
        try {
            // JSON 문자열 -> JsonNode
            JsonNode nodeContent = new ObjectMapper().readTree(content);
            // "blocks" 필드에서 배열 데이터 추출
            JsonNode blocks = nodeContent.get("blocks");

            // 각 JsonNode에서 "text" 필드 추출
            return StreamSupport.stream(blocks.spliterator(), false)
                    .map(block -> block.get("text").asText())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));
        } catch (JsonProcessingException e) {
            // JSON 파싱 실패 시 원본 content 반환
            return content;
        }
    }
}
