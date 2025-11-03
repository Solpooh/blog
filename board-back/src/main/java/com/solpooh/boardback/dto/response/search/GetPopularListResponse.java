package com.solpooh.boardback.dto.response.search;

import java.util.List;

public record GetPopularListResponse(
        List<String> popularWordList
){ }
