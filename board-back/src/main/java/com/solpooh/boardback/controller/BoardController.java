package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.request.board.PatchBoardRequestDto;
import com.solpooh.boardback.dto.request.board.PatchCommentRequestDto;
import com.solpooh.boardback.dto.request.board.PostBoardRequestDto;
import com.solpooh.boardback.dto.request.board.PostCommentRequestDto;
import com.solpooh.boardback.dto.response.board.*;
import com.solpooh.boardback.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/{boardNumber}")
    public ResponseEntity<? super GetBoardResponseDto> getBoard(
            @PathVariable("boardNumber") Integer boardNumber
    ) {
        ResponseEntity<? super GetBoardResponseDto> response = boardService.getBoardDetail(boardNumber);
        return response;
    }

    @GetMapping("/{boardNumber}/favorite-list")
    public ResponseEntity<? super GetFavoriteListResponseDto> getFavoriteList(
            @PathVariable("boardNumber") Integer boardNumber
    ) {
        ResponseEntity<? super GetFavoriteListResponseDto> response = boardService.getFavoriteList(boardNumber);
        return response;
    }

    @GetMapping("/{boardNumber}/comment-list")
    public ResponseEntity<? super GetCommentListResponseDto> getCommentList(
            @PathVariable("boardNumber") Integer boardNumber
    ) {
        ResponseEntity<? super GetCommentListResponseDto> response = boardService.getCommentList(boardNumber);
        return response;
    }

    @GetMapping("/{boardNumber}/increase-view-count")
    public ResponseEntity<? super IncreaseViewCountResponseDto> increaseViewCount(
            @PathVariable("boardNumber") Integer boardNumber
    ) {
        ResponseEntity<? super IncreaseViewCountResponseDto> response = boardService.increaseViewCount(boardNumber);
        return response;
    }

    @GetMapping("/latest-list/{category}")
    public ResponseEntity<? super GetLatestBoardListResponseDto> getLatestBoardList(
            @PathVariable(required = false) String category,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        ResponseEntity<? super GetLatestBoardListResponseDto> response = boardService.getLatestBoardList(category, pageable);
        return response;
    }

//    @GetMapping("/top-3")
//    public ResponseEntity<? super GetTop3BoardListResponseDto> getTop3BoardList() {
//        ResponseEntity<? super GetTop3BoardListResponseDto> response = boardService.getTop3BoardList();
//        return response;
//    }
//
//    @GetMapping(value = {"/search-list/{searchWord}", "/search-list/{searchWord}/{preSearchWord}"})
//    public ResponseEntity<? super GetSearchBoardListResponseDto> getSearchBoardList(
//            @PathVariable("searchWord") String searchWord,
//            @PathVariable(value = "preSearchWord", required = false) String preSearchWord
//    ) {
//        ResponseEntity<? super GetSearchBoardListResponseDto> response = boardService.getSearchBoardList(searchWord, preSearchWord);
//        return response;
//    }
//
//    @GetMapping("/user-board-list/{email}")
//    public ResponseEntity<? super GetUserBoardListResponseDto> getUserBoardList(
//            @PathVariable("email") String email
//    ) {
//        ResponseEntity<? super GetUserBoardListResponseDto> response = boardService.getUserBoardList(email);
//        return response;
//    }

    @PostMapping("")
    public ResponseEntity<? super PostBoardResponseDto> postBoard(
            @RequestBody @Valid PostBoardRequestDto requestBody,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super PostBoardResponseDto> response = boardService.postBoard(requestBody, email);
        return response;
    }

    @PostMapping("/{boardNumber}/comment")
    public ResponseEntity<? super PostCommentResponseDto> postComment(
            @RequestBody @Valid PostCommentRequestDto requestBody,
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super PostCommentResponseDto> response = boardService.postComment(requestBody, boardNumber, email);
        return response;
    }

    @PutMapping("/{boardNumber}/favorite")
    public ResponseEntity<? super PutFavoriteResponseDto> putFavorite(
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super PutFavoriteResponseDto> response = boardService.putFavorite(boardNumber, email);
        return response;
    }

    @PatchMapping("/{boardNumber}")
    public ResponseEntity<? super PatchBoardResponseDto> patchBoard(
            @RequestBody @Valid PatchBoardRequestDto requestBody,
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super PatchBoardResponseDto> response = boardService.patchBoard(requestBody, boardNumber, email);
        return response;
    }

    @PatchMapping("/{boardNumber}/comment/{commentNumber}")
    public ResponseEntity<? super PatchCommentResponseDto> patchComment(
            @RequestBody @Valid PatchCommentRequestDto requestBody,
            @PathVariable("boardNumber") Integer boardNumber,
            @PathVariable("commentNumber") Integer commentNumber,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super PatchCommentResponseDto> response = boardService.patchComment(requestBody, boardNumber, commentNumber, email);
        return response;
    }

    @DeleteMapping("/{boardNumber}")
    public ResponseEntity<? super DeleteBoardResponseDto> deleteBoard(
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super DeleteBoardResponseDto> response = boardService.deleteBoard(boardNumber, email);
        return response;
    }

    @DeleteMapping("/{boardNumber}/comment/{commentNumber}")
    public ResponseEntity<? super DeleteCommentResponseDto> deleteComment(
            @PathVariable("boardNumber") Integer boardNumber,
            @PathVariable("commentNumber") Integer commentNumber,
            @AuthenticationPrincipal String email
    ) {
        ResponseEntity<? super DeleteCommentResponseDto> response = boardService.deleteComment(boardNumber, commentNumber, email);
        return response;
    }
}
