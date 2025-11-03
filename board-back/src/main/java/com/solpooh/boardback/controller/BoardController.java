package com.solpooh.boardback.controller;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.request.board.PatchBoardRequest;
import com.solpooh.boardback.dto.request.board.PatchCommentRequest;
import com.solpooh.boardback.dto.request.board.PostBoardRequest;
import com.solpooh.boardback.dto.request.board.PostCommentRequest;
import com.solpooh.boardback.dto.response.ResponseDto;
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
    public ResponseDto<GetBoardDetailResponse> getBoardDetail(
            @PathVariable("category") String category,
            @PathVariable("boardNumber") Long boardNumber
    ) {
        GetBoardDetailResponse response = boardService.getBoardDetail(category, boardNumber);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping("/{boardNumber}/favorite-list")
    public ResponseDto<GetFavoriteListResponse> getFavoriteList(
            @PathVariable("boardNumber") Long boardNumber
    ) {
        GetFavoriteListResponse response = boardService.getFavoriteList(boardNumber);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping("/{boardNumber}/comment-list")
    public ResponseDto<GetCommentListResponse> getCommentList(
            @PathVariable("boardNumber") Long boardNumber,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        GetCommentListResponse response = boardService.getCommentList(boardNumber, pageable);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping("/{boardNumber}/increase-view-count")
    public ResponseDto<IncreaseViewCountResponse> increaseViewCount(
            @PathVariable("boardNumber") Long boardNumber
    ) {
        boardService.increaseViewCount(boardNumber);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @GetMapping("/latest-list/{category}")
    public ResponseDto<GetLatestBoardListResponse> getLatestBoardList(
            @PathVariable(required = false) String category,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        GetLatestBoardListResponse response = boardService.getLatestBoardList(category, pageable);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping("/top-3")
    public ResponseDto<GetTop3BoardListResponse> getTop3BoardList() {
        GetTop3BoardListResponse response = boardService.getTop3BoardList();
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping(value = {"/search-list/{searchWord}", "/search-list/{searchWord}/{preSearchWord}"})
    public ResponseDto<GetSearchBoardListResponse> getSearchBoardList(
            @PathVariable("searchWord") String searchWord,
            @PathVariable(value = "preSearchWord", required = false) String preSearchWord,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        GetSearchBoardListResponse response = boardService.getSearchBoardList(searchWord, preSearchWord, pageable);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @GetMapping("/user-board-list/{email}")
    public ResponseDto<GetUserBoardListResponse> getUserBoardList(
            @PathVariable("email") String email,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        GetUserBoardListResponse response = boardService.getUserBoardList(email, pageable);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }

    @PostMapping("")
    public ResponseDto<PostBoardResponse> postBoard(
            @RequestBody @Valid PostBoardRequest requestBody,
            @AuthenticationPrincipal String email
    ) {
        boardService.postBoard(requestBody, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @PostMapping("/{boardNumber}/comment")
    public ResponseDto<PostCommentResponse> postComment(
            @RequestBody @Valid PostCommentRequest requestBody,
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        boardService.postComment(requestBody, boardNumber, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @PutMapping("/{boardNumber}/favorite")
    public ResponseDto<PutFavoriteResponse> putFavorite(
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        boardService.putFavorite(boardNumber, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @PatchMapping("/{boardNumber}")
    public ResponseDto<PatchBoardResponse> patchBoard(
            @RequestBody @Valid PatchBoardRequest requestBody,
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        boardService.patchBoard(requestBody, boardNumber, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @PatchMapping("/{boardNumber}/comment/{commentNumber}")
    public ResponseDto<PatchCommentResponse> patchComment(
            @RequestBody @Valid PatchCommentRequest requestBody,
            @PathVariable("boardNumber") Long boardNumber,
            @PathVariable("commentNumber") Long commentNumber,
            @AuthenticationPrincipal String email
    ) {
        boardService.patchComment(requestBody, boardNumber, commentNumber, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @DeleteMapping("/{boardNumber}")
    public ResponseDto<DeleteBoardResponse> deleteBoard(
            @PathVariable("boardNumber") Long boardNumber,
            @AuthenticationPrincipal String email
    ) {
        boardService.deleteBoard(boardNumber, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @DeleteMapping("/{boardNumber}/comment/{commentNumber}")
    public ResponseDto<DeleteCommentResponse> deleteComment(
            @PathVariable("boardNumber") Long boardNumber,
            @PathVariable("commentNumber") Long commentNumber,
            @AuthenticationPrincipal String email
    ) {
        boardService.deleteComment(boardNumber, commentNumber, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }
}
