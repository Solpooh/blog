package com.solpooh.boardback.service.implement;

import com.solpooh.boardback.dto.response.ResponseDto;
import com.solpooh.boardback.dto.response.search.GetPopularListResponseDto;
import com.solpooh.boardback.dto.response.search.GetRelationListResponseDto;
import com.solpooh.boardback.repository.SearchLogRepository;
import com.solpooh.boardback.repository.resultSet.GetPopularListResultSet;
import com.solpooh.boardback.repository.resultSet.GetRelationListResultSet;
import com.solpooh.boardback.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImplement implements SearchService {
    private final SearchLogRepository searchLogRepository;
    @Override
    public ResponseEntity<? super GetPopularListResponseDto> getPopularList() {
        List<GetPopularListResultSet> resultSets = new ArrayList<>();

        try {

            resultSets = searchLogRepository.getPopularList();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return GetPopularListResponseDto.success(resultSets);
    }

    @Override
    public ResponseEntity<? super GetRelationListResponseDto> getRelationList(String searchWord) {
        List<GetRelationListResultSet> resultSets = new ArrayList<>();

        try {

            resultSets = searchLogRepository.getRelationList(searchWord);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return GetRelationListResponseDto.success(resultSets);
    }
}
