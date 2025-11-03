package com.solpooh.boardback.dto.request.board;

import jakarta.validation.constraints.NotBlank;

public record PostCommentRequest(
        @NotBlank
        String content
) { }
