package com.solpooh.boardback.dto.object;

public record CommentResponse(
        Long commentNumber,
        String nickname,
        String profileImage,
        String writeDatetime,
        String content,
        String userEmail
) { }
