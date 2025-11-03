package com.solpooh.boardback.dto.response.search;

import java.util.List;

public record GetRelationListResponse(
        List<String> relativeWordList
) { }
