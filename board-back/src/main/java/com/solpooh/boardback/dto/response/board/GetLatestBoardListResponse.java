package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.dto.common.BoardListResponse;

import java.util.List;

public record GetLatestBoardListResponse(
    Pagination<BoardListResponse> boardList,
    List<CategoryResponse> categoryList
){ }
