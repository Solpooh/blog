package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.board.PostBoardRequestDto;
import com.solpooh.boardback.dto.response.board.PostBoardResponseDto;
import org.springframework.http.ResponseEntity;

public interface BoardService {
    ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, String email);
}
