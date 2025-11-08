package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.dto.common.CommentResponse;


public record GetCommentListResponse(
        Pagination<CommentResponse> commentList
){ }
