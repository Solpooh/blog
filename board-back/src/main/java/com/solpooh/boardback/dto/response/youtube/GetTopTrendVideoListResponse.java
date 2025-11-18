package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.dto.common.VideoListResponse;

import java.util.List;

public record GetTopTrendVideoListResponse(
        List<VideoListResponse> videoList
) { }