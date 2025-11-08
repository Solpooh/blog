package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.dto.common.VideoListResponse;

public record GetVideoListResponse(
        Pagination<VideoListResponse> videoList
){ }
