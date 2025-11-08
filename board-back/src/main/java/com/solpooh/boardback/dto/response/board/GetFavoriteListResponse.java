package com.solpooh.boardback.dto.response.board;

import com.solpooh.boardback.dto.common.FavoriteResponse;

import java.util.List;

public record GetFavoriteListResponse(
        List<FavoriteResponse> favoriteList
){ }
