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
        return boardService.getBoardDetail(boardNumber);
    }

    @GetMapping("/{boardNumber}/favorite-list")
    public ResponseEntity<? super GetFavoriteListResponseDto> getFavoriteList(
            @PathVariable("boardNumber") Integer boardNumber
    ) {
        return boardService.getFavoriteList(boardNumber);
    }

    @GetMapping("/{boardNumber}/comment-list")
    public ResponseEntity<? super GetCommentListResponseDto> getCommentList(
            @PathVariable("boardNumber") Integer boardNumber,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getCommentList(boardNumber, pageable);
    }

    @GetMapping("/{boardNumber}/increase-view-count")
    public ResponseEntity<? super IncreaseViewCountResponseDto> increaseViewCount(
            @PathVariable("boardNumber") Integer boardNumber
    ) {
        return boardService.increaseViewCount(boardNumber);
    }

    @GetMapping("/latest-list/{category}")
    public ResponseEntity<? super GetLatestBoardListResponseDto> getLatestBoardList(
            @PathVariable(required = false) String category,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getLatestBoardList(category, pageable);
    }

    @GetMapping("/top-3")
    public ResponseEntity<? super GetTop3BoardListResponseDto> getTop3BoardList() {
        return boardService.getTop3BoardList();
    }

    @GetMapping(value = {"/search-list/{searchWord}", "/search-list/{searchWord}/{preSearchWord}"})
    public ResponseEntity<? super GetSearchBoardListResponseDto> getSearchBoardList(
            @PathVariable("searchWord") String searchWord,
            @PathVariable(value = "preSearchWord", required = false) String preSearchWord,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getSearchBoardList(searchWord, preSearchWord, pageable);
    }

    @GetMapping("/user-board-list/{email}")
    public ResponseEntity<? super GetUserBoardListResponseDto> getUserBoardList(
            @PathVariable("email") String email,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getUserBoardList(email, pageable);
    }

    @PostMapping("")
    public ResponseEntity<? super PostBoardResponseDto> postBoard(
            @RequestBody @Valid PostBoardRequestDto requestBody,
            @AuthenticationPrincipal String email
    ) {
        return boardService.postBoard(requestBody, email);
    }

    @PostMapping("/{boardNumber}/comment")
    public ResponseEntity<? super PostCommentResponseDto> postComment(
            @RequestBody @Valid PostCommentRequestDto requestBody,
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.postComment(requestBody, boardNumber, email);
    }

    @PutMapping("/{boardNumber}/favorite")
    public ResponseEntity<? super PutFavoriteResponseDto> putFavorite(
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.putFavorite(boardNumber, email);
    }

    @PatchMapping("/{boardNumber}")
    public ResponseEntity<? super PatchBoardResponseDto> patchBoard(
            @RequestBody @Valid PatchBoardRequestDto requestBody,
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.patchBoard(requestBody, boardNumber, email);
    }

    @PatchMapping("/{boardNumber}/comment/{commentNumber}")
    public ResponseEntity<? super PatchCommentResponseDto> patchComment(
            @RequestBody @Valid PatchCommentRequestDto requestBody,
            @PathVariable("boardNumber") Integer boardNumber,
            @PathVariable("commentNumber") Integer commentNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.patchComment(requestBody, boardNumber, commentNumber, email);
    }

    @DeleteMapping("/{boardNumber}")
    public ResponseEntity<? super DeleteBoardResponseDto> deleteBoard(
            @PathVariable("boardNumber") Integer boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.deleteBoard(boardNumber, email);
    }

    @DeleteMapping("/{boardNumber}/comment/{commentNumber}")
    public ResponseEntity<? super DeleteCommentResponseDto> deleteComment(
            @PathVariable("boardNumber") Integer boardNumber,
            @PathVariable("commentNumber") Integer commentNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.deleteComment(boardNumber, commentNumber, email);
    }
}
