package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.dto.common.BoardListResponse;
import java.util.List;

public record GetTop3BoardListResponse(
        List<BoardListResponse> top3List
){ }
