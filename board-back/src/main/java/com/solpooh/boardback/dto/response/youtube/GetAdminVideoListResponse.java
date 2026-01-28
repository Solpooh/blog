package com.solpooh.boardback.dto.response.youtube;

import com.solpooh.boardback.dto.object.AdminVideoItem;

import java.util.List;

public record GetAdminVideoListResponse(
        List<AdminVideoItem> videoList,
        int currentPage,
        int totalPages,
        long totalElements
) {
}
