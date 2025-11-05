package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.request.board.PatchBoardRequest;
import com.solpooh.boardback.dto.request.board.PatchCommentRequest;
import com.solpooh.boardback.dto.request.board.PostBoardRequest;
import com.solpooh.boardback.dto.request.board.PostCommentRequest;
import com.solpooh.boardback.dto.response.board.*;
import com.solpooh.boardback.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/{category}/{boardNumber}")
    public GetBoardDetailResponse getBoardDetail(
            @PathVariable("category") String category,
            @PathVariable("boardNumber") Long boardNumber
    ) {
        return boardService.getBoardDetail(category, boardNumber);
    }

    @GetMapping("/{boardNumber}/favorite-list")
    public GetFavoriteListResponse getFavoriteList(
            @PathVariable("boardNumber") Long boardNumber
    ) {
        return boardService.getFavoriteList(boardNumber);
    }

    @GetMapping("/{boardNumber}/comment-list")
    public GetCommentListResponse getCommentList(
            @PathVariable("boardNumber") Long boardNumber,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getCommentList(boardNumber, pageable);
    }

    @GetMapping("/{boardNumber}/increase-view-count")
    public IncreaseViewCountResponse increaseViewCount(
            @PathVariable("boardNumber") Long boardNumber
    ) {
        return boardService.increaseViewCount(boardNumber);
    }

    @GetMapping("/latest-list/{category}")
    public GetLatestBoardListResponse getLatestBoardList(
            @PathVariable(required = false) String category,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getLatestBoardList(category, pageable);
    }

    @GetMapping("/top-3")
    public GetTop3BoardListResponse getTop3BoardList() {
        return boardService.getTop3BoardList();
    }

    @GetMapping(value = {"/search-list/{searchWord}", "/search-list/{searchWord}/{preSearchWord}"})
    public GetSearchBoardListResponse getSearchBoardList(
            @PathVariable("searchWord") String searchWord,
            @PathVariable(value = "preSearchWord", required = false) String preSearchWord,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getSearchBoardList(searchWord, preSearchWord, pageable);
    }

    @GetMapping("/user-board-list/{email}")
    public GetUserBoardListResponse getUserBoardList(
            @PathVariable("email") String email,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return boardService.getUserBoardList(email, pageable);
    }

    @PostMapping("")
    public PostBoardResponse postBoard(
            @RequestBody @Valid PostBoardRequest requestBody,
            @AuthenticationPrincipal String email
    ) {
        return boardService.postBoard(requestBody, email);
    }

    @PostMapping("/{boardNumber}/comment")
    public PostCommentResponse postComment(
            @RequestBody @Valid PostCommentRequest requestBody,
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.postComment(requestBody, boardNumber, email);
    }

    @PutMapping("/{boardNumber}/favorite")
    public PutFavoriteResponse putFavorite(
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.putFavorite(boardNumber, email);
    }

    @PatchMapping("/{boardNumber}")
    public PatchBoardResponse patchBoard(
            @RequestBody @Valid PatchBoardRequest requestBody,
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.patchBoard(requestBody, boardNumber, email);
    }

    @PatchMapping("/{boardNumber}/comment/{commentNumber}")
    public PatchCommentResponse patchComment(
            @RequestBody @Valid PatchCommentRequest requestBody,
            @PathVariable("boardNumber") Long boardNumber,
            @PathVariable("commentNumber") Long commentNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.patchComment(requestBody, boardNumber, commentNumber, email);
    }

    @DeleteMapping("/{boardNumber}")
    public DeleteBoardResponse deleteBoard(
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.deleteBoard(boardNumber, email);
    }

    @DeleteMapping("/{boardNumber}/comment/{commentNumber}")
    public DeleteCommentResponse deleteComment(
            @PathVariable("boardNumber") Long boardNumber,
            @PathVariable("commentNumber") Long commentNumber,
            @AuthenticationPrincipal String email
    ) {
        return boardService.deleteComment(boardNumber, commentNumber, email);
    }
}