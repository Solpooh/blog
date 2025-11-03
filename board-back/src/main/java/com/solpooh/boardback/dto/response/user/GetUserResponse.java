package com.solpooh.boardback.dto.response.user;

public record GetUserResponse(
        String email,
        String nickname,
        String profileImage
) { }
