package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.dto.common.VideoResponse;

import java.util.List;

public record GetShortsVideoListResponse(
        List<VideoResponse> videoList
) { }