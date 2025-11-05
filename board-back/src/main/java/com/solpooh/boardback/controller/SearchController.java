package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.response.search.GetPopularListResponse;
import com.solpooh.boardback.dto.response.search.GetRelationListResponse;
import com.solpooh.boardback.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/popular-list")
    public GetPopularListResponse getPopularList() {
        return searchService.getPopularList();
    }

    @GetMapping("/{searchWord}/relation-list")
    public GetRelationListResponse getRelationList(
            @PathVariable("searchWord") String searchWord
    ) {
        return searchService.getRelationList(searchWord);
    }
}