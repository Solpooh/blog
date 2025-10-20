package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.board.PatchBoardRequestDto;
import com.solpooh.boardback.dto.request.board.PatchCommentRequestDto;
import com.solpooh.boardback.dto.request.board.PostBoardRequestDto;
import com.solpooh.boardback.dto.request.board.PostCommentRequestDto;
import com.solpooh.boardback.dto.response.board.*;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface BoardService {
    ResponseEntity<? super GetBoardResponseDto> getBoardDetail(String category, Long boardNumber);
    ResponseEntity<? super GetFavoriteListResponseDto> getFavoriteList(Long boardNumber);
    ResponseEntity<? super GetCommentListResponseDto> getCommentList(Long boardNumber, Pageable pageable);
    ResponseEntity<? super GetLatestBoardListResponseDto> getLatestBoardList(String category, Pageable pageable);
    ResponseEntity<? super GetTop3BoardListResponseDto> getTop3BoardList();
    ResponseEntity<? super GetSearchBoardListResponseDto> getSearchBoardList(String searchWord, String preSearchWord, Pageable pageable);
    ResponseEntity<? super GetUserBoardListResponseDto> getUserBoardList(String email, Pageable pageable);
    ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, String email);
    ResponseEntity<? super PostCommentResponseDto> postComment(PostCommentRequestDto dto, Long boardNumber, String email);

    ResponseEntity<? super PutFavoriteResponseDto> putFavorite(Long boardNumber, String email);
    ResponseEntity<? super PatchBoardResponseDto> patchBoard(PatchBoardRequestDto dto, Long boardNumber, String email);
    ResponseEntity<? super PatchCommentResponseDto> patchComment(PatchCommentRequestDto dto, Long boardNumber, Long commentNumber, String email);
    ResponseEntity<? super IncreaseViewCountResponseDto> increaseViewCount(Long boardNumber);
    ResponseEntity<? super DeleteBoardResponseDto> deleteBoard(Long boardNumber, String email);
    ResponseEntity<? super DeleteCommentResponseDto> deleteComment(Long boardNumber, Long commentNumber, String email);
}
