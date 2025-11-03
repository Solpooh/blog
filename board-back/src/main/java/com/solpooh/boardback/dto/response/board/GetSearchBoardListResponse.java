package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.dto.object.BoardListResponse;
public record GetSearchBoardListResponse(
        Pagination<BoardListResponse> searchList
) {}
