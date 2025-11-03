package com.solpooh.boardback.dto.object;
public record BoardListResponse(
        Long boardNumber,
        String title,
        String content,
        String category,
        String boardTitleImage,
        int favoriteCount,
        int commentCount,
        int viewCount,
        String writeDatetime,
        String writerNickname,
        String writerProfileImage
) { }
