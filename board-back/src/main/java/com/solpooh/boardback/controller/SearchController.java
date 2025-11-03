package com.solpooh.boardback.controller;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.response.ResponseDto;
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
    public ResponseDto<GetPopularListResponse> getPopularList() {
        GetPopularListResponse response = searchService.getPopularList();
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping("/{searchWord}/relation-list")
    public ResponseDto<GetRelationListResponse> getRelationList(
            @PathVariable("searchWord") String searchWord
    ) {
        GetRelationListResponse response = searchService.getRelationList(searchWord);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }
}
