package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.dto.common.VideoResponse;

public record GetVideoListResponse(
        Pagination<VideoResponse> videoList,
        Long totalChannelCount
){ }
