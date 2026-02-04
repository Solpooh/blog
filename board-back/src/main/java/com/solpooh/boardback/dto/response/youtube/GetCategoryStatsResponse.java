package com.solpooh.boardback.dto.response.youtube;

import java.util.List;

public record GetCategoryStatsResponse(
        List<MainCategoryStats> categories
) {
    public record MainCategoryStats(
            String mainCategory,
            String displayName,
            Long count,
            List<SubCategoryStats> subCategories
    ) {}

    public record SubCategoryStats(
            String subCategory,
            String displayName,
            Long count
    ) {}
}
