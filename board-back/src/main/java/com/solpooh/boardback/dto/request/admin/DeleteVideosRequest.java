package com.solpooh.boardback.dto.request.admin;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DeleteVideosRequest(
        @NotEmpty(message = "삭제할 비디오 ID 목록은 필수입니다")
        List<String> videoIds
) {}
