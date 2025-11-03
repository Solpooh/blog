package com.solpooh.boardback.dto.response.board;


import java.util.List;
public record GetBoardDetailResponse(
        Long boardNumber,
        String title,
        String content,
        String category,
        List<String> boardImageList,
        String writeDatetime,
        String writerEmail,
        String writerNickname,
        String writerProfileImage
) { }
