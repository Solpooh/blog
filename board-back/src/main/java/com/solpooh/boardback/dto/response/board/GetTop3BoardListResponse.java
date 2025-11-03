package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.dto.object.BoardListResponse;
import java.util.List;

public record GetTop3BoardListResponse(
        List<BoardListResponse> top3List
){ }
