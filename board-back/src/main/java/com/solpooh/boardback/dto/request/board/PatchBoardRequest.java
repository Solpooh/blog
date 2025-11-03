package com.solpooh.boardback.dto.request.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PatchBoardRequest(
    @NotBlank
    String title,
    @NotBlank
    String content,
    @NotBlank
    String category,
    @NotNull
    List<String> boardImageList
) { }
