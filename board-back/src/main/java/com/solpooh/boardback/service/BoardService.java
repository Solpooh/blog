package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.board.PatchBoardRequest;
import com.solpooh.boardback.dto.request.board.PatchCommentRequest;
import com.solpooh.boardback.dto.request.board.PostBoardRequest;
import com.solpooh.boardback.dto.request.board.PostCommentRequest;
import com.solpooh.boardback.dto.response.board.*;
import org.springframework.data.domain.Pageable;

public interface BoardService {
    GetBoardDetailResponse getBoardDetail(String category, Long boardNumber);
    GetFavoriteListResponse getFavoriteList(Long boardNumber);
    GetCommentListResponse getCommentList(Long boardNumber, Pageable pageable);
    IncreaseViewCountResponse increaseViewCount(Long boardNumber);
    GetLatestBoardListResponse getLatestBoardList(String category, Pageable pageable);
    GetTop3BoardListResponse getTop3BoardList();
    GetSearchBoardListResponse getSearchBoardList(String searchWord, String preSearchWord, Pageable pageable);
    GetUserBoardListResponse getUserBoardList(String email, Pageable pageable);
    PostBoardResponse postBoard(PostBoardRequest dto, String email);
    PostCommentResponse postComment(PostCommentRequest dto, Long boardNumber, String email);
    PutFavoriteResponse putFavorite(Long boardNumber, String email);
    PatchBoardResponse patchBoard(PatchBoardRequest dto, Long boardNumber, String email);
    PatchCommentResponse patchComment(PatchCommentRequest dto, Long boardNumber, Long commentNumber, String email);
    DeleteBoardResponse deleteBoard(Long boardNumber, String email);
    DeleteCommentResponse deleteComment(Long boardNumber, Long commentNumber, String email);
}
