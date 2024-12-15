package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.board.PatchBoardRequestDto;
import com.solpooh.boardback.dto.request.board.PatchCommentRequestDto;
import com.solpooh.boardback.dto.request.board.PostBoardRequestDto;
import com.solpooh.boardback.dto.request.board.PostCommentRequestDto;
import com.solpooh.boardback.dto.response.board.*;
import org.springframework.http.ResponseEntity;

public interface BoardService {
    ResponseEntity<? super GetBoardResponseDto> getBoard(Integer boardNumber);
    ResponseEntity<? super GetFavoriteListResponseDto> getFavoriteList(Integer boardNumber);
    ResponseEntity<? super GetCommentListResponseDto> getCommentList(Integer boardNumber);
    ResponseEntity<? super GetLatestBoardListResponseDto> getLatestBoardList();
    ResponseEntity<? super GetTop3BoardListResponseDto> getTop3BoardList();
    ResponseEntity<? super GetSearchBoardListResponseDto> getSearchBoardList(String searchWord, String preSearchWord);
    ResponseEntity<? super GetUserBoardListResponseDto> getUserBoardList(String email);
    ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, String email);
    ResponseEntity<? super PostCommentResponseDto> postComment(PostCommentRequestDto dto, Integer boardNumber, String email);

    ResponseEntity<? super PutFavoriteResponseDto> putFavorite(Integer boardNumber, String email);
    ResponseEntity<? super PatchBoardResponseDto> patchBoard(PatchBoardRequestDto dto, Integer boardNumber, String email);
    ResponseEntity<? super PatchCommentResponseDto> patchComment(PatchCommentRequestDto dto, Integer boardNumber, Integer commentNumber, String email);
    ResponseEntity<? super IncreaseViewCountResponseDto> increaseViewCount(Integer boardNumber);
    ResponseEntity<? super DeleteBoardResponseDto> deleteBoard(Integer boardNumber, String email);
    ResponseEntity<? super DeleteCommentResponseDto> deleteComment(Integer boardNumber, Integer commentNumber, String email);
}
