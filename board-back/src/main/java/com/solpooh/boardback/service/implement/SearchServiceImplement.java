package com.solpooh.boardback.service.implement;

import com.solpooh.boardback.dto.response.search.GetPopularListResponse;
import com.solpooh.boardback.dto.response.search.GetRelationListResponse;
import com.solpooh.boardback.repository.SearchLogRepository;
import com.solpooh.boardback.repository.resultSet.GetPopularListResultSet;
import com.solpooh.boardback.repository.resultSet.GetRelationListResultSet;
import com.solpooh.boardback.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImplement implements SearchService {
    private final SearchLogRepository searchLogRepository;

    @Override
    public GetPopularListResponse getPopularList() {
        List<String> popularWordList = searchLogRepository.getPopularList()
                .stream()
                .map(GetPopularListResultSet::getSearchWord)
                .toList();

        return new GetPopularListResponse(popularWordList);
    }

    @Override
    public GetRelationListResponse getRelationList(String searchWord) {

        List<String> relativeWordList = searchLogRepository.getRelationList(searchWord)
                .stream()
                .map(GetRelationListResultSet::getSearchWord)
                .toList();

        return new GetRelationListResponse(relativeWordList);
    }
}
