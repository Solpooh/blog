package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.search.GetPopularListResponse;
import com.solpooh.boardback.dto.response.search.GetRelationListResponse;

public interface SearchService {
    GetPopularListResponse getPopularList();
    GetRelationListResponse getRelationList(String searchWord);
}
